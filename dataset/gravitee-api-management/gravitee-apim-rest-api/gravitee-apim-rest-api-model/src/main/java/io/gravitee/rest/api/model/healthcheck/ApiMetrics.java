/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.model.healthcheck;

import java.util.Map;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApiMetrics<T extends Number> {

    private Map<String, Double> global;

    private Map<String, Map<String, T>> buckets;

    private Map<String, Map<String, String>> metadata;

    public Map<String, Double> getGlobal() {
        return global;
    }

    public void setGlobal(Map<String, Double> global) {
        this.global = global;
    }

    public Map<String, Map<String, T>> getBuckets() {
        return buckets;
    }

    public void setBuckets(Map<String, Map<String, T>> buckets) {
        this.buckets = buckets;
    }

    public Map<String, Map<String, String>> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Map<String, String>> metadata) {
        this.metadata = metadata;
    }
}
