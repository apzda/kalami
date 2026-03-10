/*
 * Copyright 2026 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apzda.hajimi.oss.resumable;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class ResumableInfo {

    public int resumableChunkSize;

    public long resumableTotalSize;

    public String resumableIdentifier;

    public String resumableFilename;

    public static class ResumableChunkNumber {

        public ResumableChunkNumber(int number) {
            this.number = number;
        }

        public int number;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ResumableChunkNumber && ((ResumableChunkNumber) obj).number == this.number;
        }

        @Override
        public int hashCode() {
            return number;
        }

    }

    public HashSet<ResumableChunkNumber> uploadedChunks = new HashSet<>();

    public String resumableFilePath;

    public boolean valid() {
        return resumableChunkSize >= 0 && resumableTotalSize >= 0 && StringUtils.isNotEmpty(resumableIdentifier)
                && StringUtils.isNotEmpty(resumableFilename);
    }

    public boolean checkIfUploadFinished() {
        // check if upload finished
        int count = (int) Math.ceil(((double) resumableTotalSize) / ((double) resumableChunkSize));
        for (int i = 1; i < count; i++) {
            if (!uploadedChunks.contains(new ResumableChunkNumber(i))) {
                return false;
            }
        }

        return true;
    }

}
