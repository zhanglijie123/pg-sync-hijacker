package com.github.royalflushdtd.pgsynchijacker;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.royalflushdtd.pgsynchijacker.config.YmlSubscribeConfig;
import com.github.royalflushdtd.pgsynchijacker.config.fileloader.ConfigLoader;
import com.github.royalflushdtd.pgsynchijacker.config.ConfigLoaderFactory;
import com.github.royalflushdtd.pgsynchijacker.config.JdbcConfig;
import com.github.royalflushdtd.pgsynchijacker.config.KafkaConfig;
import com.github.royalflushdtd.pgsynchijacker.config.SubscribeConfig;
import com.github.royalflushdtd.pgsynchijacker.config.PgSyncConfig;
import com.github.royalflushdtd.pgsynchijacker.config.ZkConfig;
import com.github.royalflushdtd.pgsynchijacker.constants.Constants;
import com.github.royalflushdtd.pgsynchijacker.filter.ColumnFilter;
import com.github.royalflushdtd.pgsynchijacker.filter.IEventFilter;
import com.github.royalflushdtd.pgsynchijacker.filter.SchemaFilter;
import com.github.royalflushdtd.pgsynchijacker.filter.TableNameFilter;
import com.github.royalflushdtd.pgsynchijacker.publisher.PublisherManager;
import com.github.royalflushdtd.pgsynchijacker.publisher.kafka.KafkaPublisher;

/**
 * @author machunxiao 2018-11-07
 */
public class PgSyncStarter {

    private static final Logger                         /**/ LOGGER         /**/ = LoggerFactory.getLogger(PgSyncStarter.class);

    private static final PgSyncConfig                   /**/ PG_SYNC_CONFIG  /**/ = new PgSyncConfig();

    public static void main(String[] args) {

        Map<String, String> cmdArgs = toMap(args);
        initConfig(cmdArgs);
        initMonitor(cmdArgs);

        ConfigLoader config = ConfigLoaderFactory.getConfigLoader(PG_SYNC_CONFIG);

        String configValue = config.getProperty(Constants.PG_SYNC_SUBSCRIBE_CONFIG, "");
        if ("".equals(configValue)) {
            LOGGER.warn("config is null at first setup");
            System.exit(0);
        }

        LOGGER.info("config value:{}", configValue);
        String zkAddress = config.getProperty(Constants.PG_SYNC_ZOOKEEPER_ADDRESS, "");
        if ("".equals(zkAddress)) {
            LOGGER.warn("zk address is null");
            System.exit(0);
        }
        ZkConfig zkConfig = new ZkConfig();
        zkConfig.setAddress(zkAddress);
        LOGGER.info("zk address:{}", zkAddress);

        startSubscribe(zkConfig, configValue);
        config.addChangeListener(((key, oldValue, newValue) -> {
            if (!Constants.PG_SYNC_SUBSCRIBE_CONFIG.equals(key)) {
                return;
            }
            startSubscribe(zkConfig, newValue);
        }));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
           PgSyncManager.close();
            LogManager.shutdown();
            LOGGER.info("PgSyncServer Stopped");
        }));

        LOGGER.info("PgSyncServer Started at:{}", PG_SYNC_CONFIG.getProcessId());

    }

    public static PgSyncConfig getPgSyncConfig() {
        return PG_SYNC_CONFIG;
    }

    /**
     * 初始化配置
     * <pre>
     *     -d domain
     *     -a app id
     *     -u use apollo
     *     -c config file
     *     -y use yukon
     * </pre>
     *
     * @param cfg 参数
     */
    private static void initConfig(Map<String, String> cfg) {
        PG_SYNC_CONFIG.setProcessId(getPid());
        PG_SYNC_CONFIG.setAppId(Constants.APP_ID);
        PG_SYNC_CONFIG.setConfigFile(cfg.getOrDefault("-c",Constants.CONFIG_PATH));
    }

    /**
     * <pre>
     *     -p port
     *     -m metric name
     *     -n logger name
     *     -l labels
     * </pre>
     *
     * @param cfg 参数
     */
    private static void initMonitor(Map<String, String> cfg) {
        ExporterConfig config = new ExporterConfig();
        String port = cfg.get("-p");
        String metricName = cfg.get("-m");
        String loggerName = cfg.get("-n");
        String labelNames = cfg.get("-l");
        if (StringUtils.isNotBlank(port)) {
            try {
                config.setExportPort(Integer.parseInt(port));
            } catch (Exception e) {

            }
        }
        if (StringUtils.isNotBlank(metricName)) {
            config.setMetricName(metricName);
        }
        if (StringUtils.isNotBlank(loggerName)) {
            config.setLoggerName(loggerName);
        }
        if (StringUtils.isNotBlank(labelNames)) {
            config.setLabelNames(labelNames.split(","));
        }
        boolean useYukon = "true".equalsIgnoreCase(cfg.getOrDefault("-y", "true"));
        MonitorExporter exporter = MonitorFactory.initializeExporter(useYukon, config);

        exporter.startup();
        Runtime.getRuntime().addShutdownHook(new Thread(exporter::destroy));

    }

    private static Map<String, String> toMap(String[] args) {
        Map<String, String> cfg = new LinkedHashMap<>();
        if (args == null || args.length == 0) {
            return cfg;
        }
        for (int i = 0; i < args.length; i += 2) {
            try {
                cfg.put(args[i], args[i + 1]);
            } catch (Exception e) {
                //
            }
        }
        return cfg;
    }

    private static int getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.split("@")[0]);
    }

    private static void startSubscribe(ZkConfig zkConfig, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        YmlSubscribeConfig apolloConfig = JSON.parseObject(value, YmlSubscribeConfig.class);
        PgFullSyncServer pgFullSyncServer = new PgFullSyncServer(apolloConfig);
       // pgFullSyncServer.start();
        List<YmlSubscribeConfig.Subscribe> subscribes = apolloConfig.getSubscribes();
        for (YmlSubscribeConfig.Subscribe subscribe : subscribes) {
           PgIncrementSyncServer newServer = null;
           PgIncrementSyncServer oldServer = null;
            try {
                SubscribeConfig subscribeConfig = toSubscribeConfig(subscribe);
                subscribeConfig.setZkConfig(zkConfig);
                newServer = new PgIncrementSyncServer(subscribeConfig);
                oldServer = PgSyncManager.findServer(newServer.getServerId());
                if (oldServer != null) {
                    oldServer.shutdown();
                }
               PgSyncManager.putServer(newServer);
                newServer.start();
            } catch (Exception e) {
                LOGGER.warn("setup :'" + subscribe.getSlotName() + "' failure", e);
                //
                if (oldServer != null) {
                    oldServer.shutdown();
                   PgSyncManager.remove(oldServer.getServerId());
                }
                if (newServer != null) {
                    newServer.shutdown();
                   PgSyncManager.remove(newServer.getServerId());
                }
            }
        }
    }

    private static SubscribeConfig toSubscribeConfig(YmlSubscribeConfig.Subscribe subscribe) {

        String slotName = subscribe.getSlotName();
        YmlSubscribeConfig.KafkaConf kafkaConf = subscribe.getKafkaConf();

        YmlSubscribeConfig.PgConnConf pgConnConf = subscribe.getPgConnConf();
        List<YmlSubscribeConfig.Rule> rules = subscribe.getRules();

        parseKafkaConfig(slotName, kafkaConf, rules);
        JdbcConfig jdbcConfig = getJdbcConfig(slotName, pgConnConf);
        SubscribeConfig subscribeConfig = new SubscribeConfig();
        subscribeConfig.setJdbcConfig(jdbcConfig);
        subscribeConfig.setServerId(generateServerId(pgConnConf.getHost(), pgConnConf.getPort(), jdbcConfig.getSlotName()));
        return subscribeConfig;
    }

    /**
     * generate CONFIG_NAME new serverId
     *
     * @param host host
     * @param port port
     * @param slot slot
     * @return serverId
     */
    private static String generateServerId(String host, int port, String slot) {
        return slot + "@" + host + ":" + port;
    }

    private static void parseKafkaConfig(String slotName, YmlSubscribeConfig.KafkaConf kafkaConf, List<YmlSubscribeConfig.Rule> rules) {
        if (kafkaConf == null || CollectionUtils.isEmpty(kafkaConf.getAddrs())) {
            return;
        }

        List<KafkaConfig> kafkaConfigs = rules.stream()
                .map(PgSyncStarter::toKafkaConfig)
                .filter(Objects::nonNull)
                .peek(cfg -> cfg.setServer(StringUtils.join(kafkaConf.getAddrs(), ",")))
                .collect(Collectors.toList());
        PublisherManager.getInstance().putPublisher(slotName, new KafkaPublisher(kafkaConfigs));
    }







    private static KafkaConfig toKafkaConfig(YmlSubscribeConfig.Rule rule) {
        if (StringUtils.isBlank(rule.getTopic())) {
            return null;
        }
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setTopic(rule.getTopic());
        kafkaConfig.setPartition(rule.getPartition());


        List<IEventFilter> filters = new ArrayList<>();
        filters.add(new ColumnFilter(rule.getColumns()));
        filters.add(new SchemaFilter(rule.getScheme()));
        filters.add(new TableNameFilter(rule.getTable()));
        kafkaConfig.setFilters(filters);
        kafkaConfig.setKeys(new ArrayList<>(rule.getKeys()));

        return kafkaConfig;
    }



    private static JdbcConfig getJdbcConfig(String slotName, YmlSubscribeConfig.PgConnConf pgConnConf) {
        String jdbcUrl = "jdbc:postgresql://" + pgConnConf.getHost() + ":" + pgConnConf.getPort() + "/" + pgConnConf.getDatabase();
        JdbcConfig jdbcConfig = new JdbcConfig();
        jdbcConfig.setSlotName(slotName);
        jdbcConfig.setUrl(jdbcUrl);
        jdbcConfig.setUsername(pgConnConf.getUser());
        jdbcConfig.setPassword(pgConnConf.getPassword());
        jdbcConfig.setHost(pgConnConf.getHost());
        jdbcConfig.setPort(pgConnConf.getPort());
        jdbcConfig.setSchema(pgConnConf.getDatabase());
        return jdbcConfig;
    }

}
