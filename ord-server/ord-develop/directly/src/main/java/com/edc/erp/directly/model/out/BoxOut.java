package com.edc.erp.directly.model.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoxOut implements Serializable {

    private String value;

    private String viewShow;
}
