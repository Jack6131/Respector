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
package io.gravitee.rest.api.model.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.gravitee.rest.api.model.annotations.ParameterKey;
import io.gravitee.rest.api.model.parameters.Key;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortalAuthentication extends CommonAuthentication {

    @ParameterKey(Key.PORTAL_AUTHENTICATION_FORCELOGIN_ENABLED)
    private Enabled forceLogin;

    @ParameterKey(Key.PORTAL_AUTHENTICATION_LOCALLOGIN_ENABLED)
    private Enabled localLogin;

    public Enabled getForceLogin() {
        return forceLogin;
    }

    public void setForceLogin(Enabled forceLogin) {
        this.forceLogin = forceLogin;
    }

    public Enabled getLocalLogin() {
        return localLogin;
    }

    public void setLocalLogin(Enabled localLogin) {
        this.localLogin = localLogin;
    }
}
