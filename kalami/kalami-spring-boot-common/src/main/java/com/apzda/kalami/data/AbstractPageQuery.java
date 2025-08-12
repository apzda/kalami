/*
 * Copyright 2025 the original author or authors.
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
package com.apzda.kalami.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public abstract class AbstractPageQuery implements PageRequest {

    /**
     * 页码(从1开始)，默认1
     */
    private int pageNumber = 1;

    /**
     * 分页大小,默认10
     */
    private int pageSize = 20;

    /**
     * 排序(格式:field1|asc,field2|desc[,...])
     */
    private String pageSorts;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private boolean searchCount = true;

    public void searchCount(boolean searchCount) {
        this.searchCount = searchCount;
    }

    @Override
    public boolean searchCount() {
        return searchCount;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber <= 0 ? 1 : pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize <= 0 ? 20 : Math.min(100, pageSize);
    }

    @Override
    public String getPageSorts() {
        return pageSorts;
    }

    public void setPageSorts(String pageSorts) {
        this.pageSorts = pageSorts;
    }

}
