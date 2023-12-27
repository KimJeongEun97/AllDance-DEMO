package com.aidata.alldance.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class DanceDto {
    private int d_num;
    private String d_title;
    private String d_contents;
    private String d_id;
    private String m_name;
    private Timestamp d_date;
    private int d_views;
    private String if_sysname;
    private String if_oriname;
}
