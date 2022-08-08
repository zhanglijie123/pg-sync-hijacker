/*
 * Copyright 2018 Shanghai Junzheng Network Technology Co.,Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.royalflushdtd.pgsynchijacker;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
@Data
@Builder
public class Statics {

    private String appId;
    private String database;
    private String slotName;
    private String table;
    private int total;
    private String target;
    private String error;
    private long currentTime;

    public static Statics createStatics(String appId, String database, String slotName, String table, int total, String target, String error) {
        return Statics.builder()
                .appId(appId)
                .database(database)
                .slotName(slotName)
                .table(table)
                .total(total)
                .target(target)
                .error(error)
                .currentTime(System.currentTimeMillis())
                .build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(8, 1f);
        map.put("appId", getAppId());
        map.put("database", getDatabase());
        map.put("slotName", getSlotName());
        map.put("table", getTable());
        map.put("total", getTotal() + "");
        map.put("target", getTarget());
        map.put("error", getError());
        map.put("currentTime", getCurrentTime() == 0 ? System.currentTimeMillis() : getCurrentTime());
        return map;
    }
}
