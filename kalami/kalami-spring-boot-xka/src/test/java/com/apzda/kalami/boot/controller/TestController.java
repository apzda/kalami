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
package com.apzda.kalami.boot.controller;

import com.alibaba.excel.EasyExcel;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.boot.entity.User;
import com.apzda.kalami.boot.enums.TestStatus;
import com.apzda.kalami.boot.enums.TestStatus2;
import com.apzda.kalami.boot.enums.TestStatus3;
import com.apzda.kalami.boot.excel.AbstractExcelView;
import com.apzda.kalami.boot.service.IUserService;
import com.apzda.kalami.boot.vo.TestVo;
import com.apzda.kalami.data.PageRequest;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.validation.Group;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.apzda.kalami.boot.excel.AbstractExcelView.FILE_NAME;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@RestController
@RequestMapping("/user")
public class TestController extends AbstractCrudController<String, User, IUserService> {

    @Autowired
    private IUserService userService;

    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Dictionary
    public Response<Paged<User>> list(User entity, PageRequest page, HttpServletRequest req) {
        return queryPage(entity, page.toPageable(), req);
    }

    @ResponseBody
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Dictionary
    public Response<User> read(@PathVariable String id) {
        return findById(id);
    }

    @ResponseBody
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Dictionary
    public Response<User> add(@RequestBody User entity) {
        return create(entity);
    }

    @ResponseBody
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Dictionary
    @Validated(Group.Update.class)
    public Response<User> updateById(@PathVariable String id, @RequestBody User entity) {
        return update(id, entity);
    }

    @ResponseBody
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Dictionary
    public Response<User> deleteById(@PathVariable String id) {
        return delete(id);
    }

    @ResponseBody
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Dictionary
    @Transactional(rollbackFor = Exception.class)
    public Response<List<User>> deleteByIds(@RequestBody List<String> ids) {
        return delete(ids);
    }

    @Dictionary
    @GetMapping("/test/{id}")
    public Response<TestVo> testVoResponse(@PathVariable String id) {
        TestVo testVo = new TestVo();
        testVo.setName("test = " + id);
        testVo.setStatus(TestStatus.T1);
        testVo.setStatus2(TestStatus2.T2);
        testVo.setStatus3(TestStatus3.T3);
        testVo.setPhone("13166666666");
        testVo.setPhone1("13166666666");
        testVo.setPhone2("13166666666");
        return Response.success(testVo);
    }

    @Dictionary
    public Response<List<User>> getUserList() {
        return Response.success(userService.list());
    }

    @Dictionary
    public Response<IPage<User>> getUserPage() {
        IPage<User> page = new Page<>();
        page.setSize(100L);
        page.setCurrent(1L);
        return Response.success(userService.page(page));
    }

    @GetMapping("/export")
    public ModelAndView exportXlsx() {
        val list = userService.list();
        return new ModelAndView(new AbstractExcelView() {
            @Override
            protected void doExport(@Nonnull Map<String, Object> model, @Nonnull HttpServletRequest request,
                    @Nonnull HttpServletResponse response) throws IOException {

                EasyExcel.write(response.getOutputStream(), User.class).sheet("模板").doWrite(list);
            }
        }).addObject(FILE_NAME, "test");
    }

}
