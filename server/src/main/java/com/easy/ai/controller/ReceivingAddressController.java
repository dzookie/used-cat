package com.easy.ai.controller;

import com.easy.ai.entity.ReceivingAddress;
import com.easy.ai.common.Result;
import com.easy.ai.dto.AddReceivingAddressDTO;
import com.easy.ai.service.ReceivingAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/receivingAddress")
@Tag(name = "收货地址", description = "收货地址相关接口")
@Validated
public class ReceivingAddressController {

    @Autowired
    private ReceivingAddressService receivingAddressService;

    @GetMapping("/getReceivingAddressList")
    @Operation(summary = "获取收货地址列表", description = "根据用户id获取收货地址列表")
    public Result<List<ReceivingAddress>> getAddressList(@RequestParam("userId") Integer userId) {
        List<ReceivingAddress> addressList = receivingAddressService.lambdaQuery()
                .eq(ReceivingAddress::getUserId, userId)
                .list();
        return Result.success("获取收货地址列表成功！", addressList);
    }

    @PostMapping("/addReceivingAddress")
    @Operation(summary = "创建收货地址", description = "创建新的收货地址")
    public Result<Void> addAddress(@RequestBody AddReceivingAddressDTO dto) {
        ReceivingAddress address = new ReceivingAddress();
        address.setUserId(dto.getUserId());
        address.setConsignee(dto.getConsignee());
        address.setPhone(dto.getPhone());
        address.setRegion(dto.getRegion());
        address.setAddress(dto.getAddress());

        boolean success = receivingAddressService.save(address);
        if (success) {
            return Result.success("创建收货地址成功！", null);
        } else {
            return Result.error("创建收货地址失败");
        }
    }

    @PutMapping("/update")
    @Operation(summary = "修改收货地址", description = "根据id修改收货地址")
    public Result<Void> updateAddress(@RequestBody ReceivingAddress address) {
        boolean success = receivingAddressService.updateById(address);
        if (success) {
            return Result.success("修改收货地址成功！", null);
        } else {
            return Result.error("修改收货地址失败");
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收货地址", description = "根据id删除收货地址")
    public Result<Void> deleteAddress(@RequestParam("id") Integer id) {
        boolean success = receivingAddressService.removeById(id);
        if (success) {
            return Result.success("删除收货地址成功！", null);
        } else {
            return Result.error("删除收货地址失败");
        }
    }

}
