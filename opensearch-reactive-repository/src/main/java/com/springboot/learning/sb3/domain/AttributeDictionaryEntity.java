package com.springboot.learning.sb3.domain;

import java.util.List;
import java.util.Map;

public record AttributeDictionaryEntity(

    String id,
    String code,
    String type,
    String group,
    boolean unique,
    boolean useableAsGridFilter,
    List<String> allowedExtensions,
    String metricFamily,
    String defaultMetricUnit,
    String referenceDataName,
    List<String> availableLocales,
    Integer maxCharacters,
    String validationRule,
    String validationRegexp,
    Boolean wysiwygEnabled,
    Double numberMin,
    Double numberMax,
    Boolean decimalsAllowed,
    Boolean negativeAllowed,
    String dateMin,
    String dateMax,
    Integer maxFileSize,
    Integer minimumInputLength,
    int sortOrder,
    boolean localizable,
    boolean scopable,
    Map<String,String> labels,
    List<String> guidelines,
    Boolean autoOptionSorting,
    String defaultValue,
    boolean isReadOnly) {}
