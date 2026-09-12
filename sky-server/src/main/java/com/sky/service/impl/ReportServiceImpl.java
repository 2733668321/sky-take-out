package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;


    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        do {
            dateList.add(begin);
            begin = begin.plusDays(1);
        } while (!begin.isAfter(end));

        String dateListStr = StringUtils.join(dateList, ",");

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询date日期对对应的营业额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        String turnoverListStr = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO
                .builder()
                .dateList(dateListStr)
                .turnoverList(turnoverListStr)
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        do {
            dateList.add(begin);
            begin = begin.plusDays(1);
        } while (!begin.isAfter(end));

        String dateListStr = StringUtils.join(dateList, ",");

        //每天新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //每天总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询date日期新增用户数量和总用户数量
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();

            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        String totalUserListStr = StringUtils.join(totalUserList, ",");

        String newUserListStr = StringUtils.join(newUserList, ",");
        return UserReportVO
                .builder()
                .dateList(dateListStr)
                .totalUserList(totalUserListStr)
                .newUserList(newUserListStr)
                .build();
    }
}
