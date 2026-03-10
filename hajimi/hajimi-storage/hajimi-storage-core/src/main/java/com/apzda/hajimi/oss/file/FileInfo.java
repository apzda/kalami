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
package com.apzda.hajimi.oss.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileInfo {

    private int index; // 第几个文件

    private String url; // 文件的URL

    private String path; // 文件的绝对路径

    private String backend;// 文件的后端存储

    private Integer error; // 0: 正常; 其它值出错

    private String message; // 当error!=0时的错误提示

    private Boolean exist; // 文件是否存在，用于查询

    private long length;// 文件长度

    private String fileId;// 文件ID

    private String ext;

    private String filename;

    private long createTime;

    private String contentType;

}
