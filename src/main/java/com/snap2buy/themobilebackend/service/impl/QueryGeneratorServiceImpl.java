package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.mapper.ConfMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.service.QueryGenerationService;
import com.snap2buy.themobilebackend.util.QueryDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Iterator;
import java.util.Map.Entry;

//@Component(value = BeanMapper.BEAN_QUERY_GENERATION_SERVICE)
public class QueryGeneratorServiceImpl implements
        QueryGenerationService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value(value = ConfMapper._1)
    private String _1Template;

    @Value(value = ConfMapper._1HEADERS)
    private String _1Headers;

    @Value(value = ConfMapper._1DISPLAY_HEADERS)
    private String _1DisplayHeaders;

    @Override
    public String getHeaders(InputObject input) {
        String queryHeaders;
        int queryId = Integer.parseInt(input.getQueryId());

        switch (queryId) {
            case 1:
                LOGGER.info("in bob overview headers");
                queryHeaders = _1Headers;
                break;
            default:
                LOGGER.info("in default");
                queryHeaders = null;
        }
        return queryHeaders;
    }

    @Override
    public String generateQuery(InputObject input) {

        String queryTemplate;
        int queryId = Integer.parseInt(input.getQueryId());

        switch (queryId) {
            case 1:
                queryTemplate = _1Template;
                queryTemplate = DateTableTemplating(input, queryTemplate);
                queryTemplate = limitTemplating(input, queryTemplate);
                queryTemplate = moreFiltersTemplating(input, queryTemplate);
                break;

            default:
                LOGGER.info("in default");
                queryTemplate = null;
        }

        return queryTemplate;

    }

    //
    private String DateTableTemplating(InputObject input, String queryTemplate) {

        try {
            QueryDateUtil.setBaseDates(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dateColumn = "dateId";
        String tableName = "ShelfAnalysis";

        if (queryTemplate.contains("#dateColumn#")) {
            queryTemplate = queryTemplate
                    .replaceAll("#dateColumn#", dateColumn);
        }
        if (queryTemplate.contains("#tableName#")) {
            queryTemplate = queryTemplate
                    .replaceAll("#tableName#", tableName);
        }
        if (queryTemplate.contains("#start_time#")) {
            queryTemplate = queryTemplate.replaceAll("#start_time#",
                    input.getStartTime());
        }
        if (queryTemplate.contains("#end_time#")) {
            queryTemplate = queryTemplate.replaceAll("#end_time#",
                    input.getEndTime());
        }

        if (queryTemplate.contains("#prev_start_time#")) {
            queryTemplate = queryTemplate.replaceAll("#prev_start_time#",
                    input.getPrevStartTime());
        }
        if (queryTemplate.contains("#prev_end_time#")) {
            queryTemplate = queryTemplate.replaceAll("#prev_end_time#",
                    input.getPrevEndTime());
        }

        System.out.print("------->>" + queryTemplate + input);

        return queryTemplate;
    }

    public String limitTemplating(InputObject input, String queryTemplate) {

        if (queryTemplate.contains("#limit#")) {
            queryTemplate = queryTemplate.replaceAll("#limit#",
                    input.getLimit());
        }
        return queryTemplate;

    }


    public String moreFiltersTemplating(InputObject input, String queryTemplate) {
        StringBuilder whereClause = new StringBuilder();

        if (input.getMorefiltersMap() != null) {
            Iterator iter = input.getMorefiltersMap().entrySet().iterator();
            while (iter.hasNext()) {
                Entry mEntry = (Entry) iter.next();
                System.out.println("IN MoreFilters ---->>" + mEntry.getKey() + " : " + mEntry.getValue());

                if (mEntry.getKey() != null && (mEntry.getValue() != null) && (mEntry.getValue().toString().split(",").length < 2)) {
                    StringBuilder condition = new StringBuilder();
                    condition.append(" AND ").append(mEntry.getKey()).append(" = '").append(mEntry.getValue()).append("' ");
                    whereClause.append(condition.toString());
                } else {
                    StringBuilder condition = new StringBuilder();
                    condition.append(" AND ").append(mEntry.getKey()).append(" in ('").append(mEntry.getValue().toString().replaceAll(",", "','")).append("') ");
                    whereClause.append(condition.toString());
                }
            }
        }
        queryTemplate = queryTemplate.replaceAll("#morefilters#", whereClause.toString());
        return queryTemplate;
    }


    public static void main(String args[]) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "base-spring-ctx.xml");
        /*AudienceQueryGeneratorServiceImpl impl = (AudienceQueryGeneratorServiceImpl) ctx
				.getBean("AudienceQueryGeneratorServiceImpl");*/


        InputObject input = new InputObject();

        input.setStartDate("20160201");
        input.setEndDate("20160220");
        input.setUserId("agsachin");
        input.setLimit("10");
        input.setFrequency("DAILY");
        input.setQueryId("1");
        input.setCategoryId("shampoo");

        QueryGeneratorServiceImpl impl = new QueryGeneratorServiceImpl();


        String qtemplate = "";
    }


}
