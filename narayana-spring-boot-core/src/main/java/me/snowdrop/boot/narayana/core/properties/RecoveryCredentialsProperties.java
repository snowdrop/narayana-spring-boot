/*
 * Copyright 2020 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.snowdrop.boot.narayana.core.properties;

public class RecoveryCredentialsProperties {

    /**
     * default instance for convenience.
     */
    public static final RecoveryCredentialsProperties DEFAULT;

    private String user;
    private String password;

    static {
        DEFAULT = new RecoveryCredentialsProperties();
    }

    public RecoveryCredentialsProperties() {
    }

    public RecoveryCredentialsProperties(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public boolean isValid() {
        return !(this.user == null && this.password == null);
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
