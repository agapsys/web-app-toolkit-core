/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agapsys.web.toolkit;

import com.agapsys.web.toolkit.modules.LogModule;
import com.agapsys.web.toolkit.modules.LogModule.ConsoleLogStream;
import com.agapsys.web.toolkit.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebListener;

@WebListener
public class MockedWebApplication extends AbstractWebApplication {

    @Override
    public String getName() {
        return "test-app";
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    protected File getParentDir() {
        return FileUtils.DEFAULT_TEMPORARY_FOLDER;
    }

    @Override
    protected void afterApplicationStop() {
        super.afterApplicationStop();

        try {
            super.afterApplicationStop();
            FileUtils.deleteFile(getDirectory());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void beforeApplicationStart() {
        super.beforeApplicationStart();

        LogModule logModule = getModule(LogModule.class);
        logModule.addStream(new ConsoleLogStream());
    }

}
