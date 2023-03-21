package ru.test;

import java.util.Arrays;
import java.util.HashMap;

public class FormDataAnalys
{

        private byte[] rawData;
        private int index;
        // parameter
        private HashMap<String, String> params;
        private byte[] fileData;
        private String wrapperName;
        private String contentType;
        private int endSize;
        public FormDataAnalys (byte[] data) throws IllegalArgumentException
        {
            rawData = data;
            params = new HashMap<>();
            index = 0;
            parserData();
        }

        private void parserData() throws IllegalArgumentException {
            wrapperName = readLine();
            String params = readLine();
            parserParams(params);
            contentType = readLine().split(": ")[1];
            eatRedundantLine();
// read data
            fileData = Arrays.copyOfRange(rawData, index
                    , rawData.length - wrapperName.length() - 2 - endSize - endSize);
        }

        private void parserParams(String params) {
            String[] splits = params.split("; ");
            for (int i = 1; i < splits.length; i++) {
                String one = splits[i];
                String[] oneSplits = one.split("=");
                this.params.put(oneSplits[0], oneSplits[1].substring(1, oneSplits[1].length()-1));
            }
        }

        public HashMap<String, String> getParams() {
            return params;
        }

        public byte[] getFileData() {
            return fileData;
        }

        public String getWrapperName() {
            return wrapperName;
        }

        public String getContentType() {
            return contentType;
        }
        private String readLine() {
            boolean done = false;
            int startIndex = index;
            int count = 0;
            while (!done) {
                if (isLineEnd()) {
                    done = true;
                } else {
                    index++;
                    count++;
                }
            }

            try {
// TODO code self-contracted, default UTF-8
                return new String(rawData, startIndex, count, "utf-8");
            }
            catch (Exception e) {
                return new String(rawData, startIndex, count);
            }
        }

        private boolean isLineEnd() {
            if (rawData[index] == '\r' && rawData[index+1] == '\n') {
                index+=2;
                endSize = 2;
                return true;
            } else if (rawData[index] == '\n') {
                index++;
                endSize = 1;
                return true;
            } else if (rawData[index] == '\r') {
                index++;
                endSize = 1;
                return true;
            } else {
                return false;
            }
        }

        private void eatRedundantLine() {
            while(isLineEnd()) {};
        }
    }

