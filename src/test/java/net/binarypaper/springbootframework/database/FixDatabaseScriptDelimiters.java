/*
 * Copyright 2016 <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>.
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
package net.binarypaper.springbootframework.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Utility class to fix the database scripts delimiters
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
public class FixDatabaseScriptDelimiters {

    private static final String CREATE_SCRIPT = "database-scripts/create-database.sql";
    private static final String DROP_SCRIPT = "database-scripts/drop-database.sql";

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            addSqlFileDelimiters(arg);
        }
    }

    public void generateDatabase() throws Exception {
        addSqlFileDelimiters(CREATE_SCRIPT);
        addSqlFileDelimiters(DROP_SCRIPT);
    }

    private static void addSqlFileDelimiters(String fileName) throws Exception {
        File file = new File(fileName);
        FileReader fileReader = new FileReader(file);
        String line;
        String output = "";
        try (BufferedReader br = new BufferedReader(fileReader)) {
            while ((line = br.readLine()) != null) {
                output += line + ";\n";
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(output);
            fileWriter.close();
        }
    }
}
