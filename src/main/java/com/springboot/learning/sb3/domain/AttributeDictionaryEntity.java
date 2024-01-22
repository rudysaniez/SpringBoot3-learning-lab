package com.springboot.learning.sb3.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Document(indexName = "attributs_dictionnary_v1")
public class AttributeDictionaryEntity {

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "code")
    private String code;

    @Field(type = FieldType.Text, name = "type")
    private String type;

    @Field(type = FieldType.Text, name = "group")
    private String group;

    @Field(type = FieldType.Boolean, name = "unique")
    private boolean unique;

    @Field(type = FieldType.Boolean, name = "useableAsGridFilter")
    private boolean useableAsGridFilter;

    @Field(type = FieldType.Nested, name = "useableAsGridFilter")
    private List<String> allowedExtensions;

    @Field(type = FieldType.Text, name = "metricFamily")
    private String metricFamily;

    @Field(type = FieldType.Text, name = "defaultMetricUnit")
    private String defaultMetricUnit;

    @Field(type = FieldType.Text, name = "referenceDataName")
    private String referenceDataName;

    @Field(type = FieldType.Nested, name = "referenceDataName")
    private List<String> availableLocales;

    @Field(type = FieldType.Integer, name = "maxCharacters")
    private Integer maxCharacters;

    @Field(type = FieldType.Text, name = "maxCharacters")
    private String validationRule;

    @Field(type = FieldType.Text, name = "validationRegexp")
    private String validationRegexp;

    @Field(type = FieldType.Boolean, name = "wysiwygEnabled")
    private Boolean wysiwygEnabled;

    @Field(type = FieldType.Double, name = "numberMin")
    private Double numberMin;

    @Field(type = FieldType.Double, name = "numberMax")
    private Double numberMax;

    @Field(type = FieldType.Boolean, name = "decimalsAllowed")
    private Boolean decimalsAllowed;

    @Field(type = FieldType.Boolean, name = "negativeAllowed")
    private Boolean negativeAllowed;

    @Field(type = FieldType.Text, name = "dateMin")
    private String dateMin;

    @Field(type = FieldType.Text, name = "dateMax")
    private String dateMax;

    @Field(type = FieldType.Integer, name = "maxFileSize")
    private Integer maxFileSize;

    @Field(type = FieldType.Integer, name = "minimumInputLength")
    private Integer minimumInputLength;

    @Field(type = FieldType.Integer, name = "sortOrder")
    private int sortOrder;

    @Field(type = FieldType.Boolean, name = "localizable")
    private boolean localizable;

    @Field(type = FieldType.Boolean, name = "scopable")
    private boolean scopable;

    @Field(type = FieldType.Nested, name = "labels")
    private Map<String,String> labels;

    @Field(type = FieldType.Nested, name = "guidelines")
    private List<String> guidelines;

    @Field(type = FieldType.Boolean, name = "autoOptionSorting")
    private Boolean autoOptionSorting;

    @Field(type = FieldType.Text, name = "defaultValue")
    private String defaultValue;

    @Field(type = FieldType.Boolean, name = "isReadOnly")
    private boolean isReadOnly;

    public AttributeDictionaryEntity() {}

    public AttributeDictionaryEntity(String id,
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
                                     Map<String, String> labels,
                                     List<String> guidelines,
                                     Boolean autoOptionSorting,
                                     String defaultValue,
                                     boolean isReadOnly) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.group = group;
        this.unique = unique;
        this.useableAsGridFilter = useableAsGridFilter;
        this.allowedExtensions = allowedExtensions;
        this.metricFamily = metricFamily;
        this.defaultMetricUnit = defaultMetricUnit;
        this.referenceDataName = referenceDataName;
        this.availableLocales = availableLocales;
        this.maxCharacters = maxCharacters;
        this.validationRule = validationRule;
        this.validationRegexp = validationRegexp;
        this.wysiwygEnabled = wysiwygEnabled;
        this.numberMin = numberMin;
        this.numberMax = numberMax;
        this.decimalsAllowed = decimalsAllowed;
        this.negativeAllowed = negativeAllowed;
        this.dateMin = dateMin;
        this.dateMax = dateMax;
        this.maxFileSize = maxFileSize;
        this.minimumInputLength = minimumInputLength;
        this.sortOrder = sortOrder;
        this.localizable = localizable;
        this.scopable = scopable;
        this.labels = labels;
        this.guidelines = guidelines;
        this.autoOptionSorting = autoOptionSorting;
        this.defaultValue = defaultValue;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String toString() {
        return "AttributeDictionaryEntity{" +
               "id='" + id + '\'' +
               ", code='" + code + '\'' +
               ", type='" + type + '\'' +
               ", group='" + group + '\'' +
               ", unique=" + unique +
               ", useableAsGridFilter=" + useableAsGridFilter +
               ", allowedExtensions=" + allowedExtensions +
               ", metricFamily='" + metricFamily + '\'' +
               ", defaultMetricUnit='" + defaultMetricUnit + '\'' +
               ", referenceDataName='" + referenceDataName + '\'' +
               ", availableLocales=" + availableLocales +
               ", maxCharacters=" + maxCharacters +
               ", validationRule='" + validationRule + '\'' +
               ", validationRegexp='" + validationRegexp + '\'' +
               ", wysiwygEnabled=" + wysiwygEnabled +
               ", numberMin=" + numberMin +
               ", numberMax=" + numberMax +
               ", decimalsAllowed=" + decimalsAllowed +
               ", negativeAllowed=" + negativeAllowed +
               ", dateMin='" + dateMin + '\'' +
               ", dateMax='" + dateMax + '\'' +
               ", maxFileSize=" + maxFileSize +
               ", minimumInputLength=" + minimumInputLength +
               ", sortOrder=" + sortOrder +
               ", localizable=" + localizable +
               ", scopable=" + scopable +
               ", labels=" + labels +
               ", guidelines=" + guidelines +
               ", autoOptionSorting=" + autoOptionSorting +
               ", defaultValue='" + defaultValue + '\'' +
               ", isReadOnly=" + isReadOnly +
               '}';
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isUseableAsGridFilter() {
        return useableAsGridFilter;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public String getMetricFamily() {
        return metricFamily;
    }

    public String getDefaultMetricUnit() {
        return defaultMetricUnit;
    }

    public String getReferenceDataName() {
        return referenceDataName;
    }

    public List<String> getAvailableLocales() {
        return availableLocales;
    }

    public Integer getMaxCharacters() {
        return maxCharacters;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public String getValidationRegexp() {
        return validationRegexp;
    }

    public Boolean getWysiwygEnabled() {
        return wysiwygEnabled;
    }

    public Double getNumberMin() {
        return numberMin;
    }

    public Double getNumberMax() {
        return numberMax;
    }

    public Boolean getDecimalsAllowed() {
        return decimalsAllowed;
    }

    public Boolean getNegativeAllowed() {
        return negativeAllowed;
    }

    public String getDateMin() {
        return dateMin;
    }

    public String getDateMax() {
        return dateMax;
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public Integer getMinimumInputLength() {
        return minimumInputLength;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isLocalizable() {
        return localizable;
    }

    public boolean isScopable() {
        return scopable;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public List<String> getGuidelines() {
        return guidelines;
    }

    public Boolean getAutoOptionSorting() {
        return autoOptionSorting;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setUseableAsGridFilter(boolean useableAsGridFilter) {
        this.useableAsGridFilter = useableAsGridFilter;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public void setMetricFamily(String metricFamily) {
        this.metricFamily = metricFamily;
    }

    public void setDefaultMetricUnit(String defaultMetricUnit) {
        this.defaultMetricUnit = defaultMetricUnit;
    }

    public void setReferenceDataName(String referenceDataName) {
        this.referenceDataName = referenceDataName;
    }

    public void setAvailableLocales(List<String> availableLocales) {
        this.availableLocales = availableLocales;
    }

    public void setMaxCharacters(Integer maxCharacters) {
        this.maxCharacters = maxCharacters;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public void setValidationRegexp(String validationRegexp) {
        this.validationRegexp = validationRegexp;
    }

    public void setWysiwygEnabled(Boolean wysiwygEnabled) {
        this.wysiwygEnabled = wysiwygEnabled;
    }

    public void setNumberMin(Double numberMin) {
        this.numberMin = numberMin;
    }

    public void setNumberMax(Double numberMax) {
        this.numberMax = numberMax;
    }

    public void setDecimalsAllowed(Boolean decimalsAllowed) {
        this.decimalsAllowed = decimalsAllowed;
    }

    public void setNegativeAllowed(Boolean negativeAllowed) {
        this.negativeAllowed = negativeAllowed;
    }

    public void setDateMin(String dateMin) {
        this.dateMin = dateMin;
    }

    public void setDateMax(String dateMax) {
        this.dateMax = dateMax;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setMinimumInputLength(Integer minimumInputLength) {
        this.minimumInputLength = minimumInputLength;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setLocalizable(boolean localizable) {
        this.localizable = localizable;
    }

    public void setScopable(boolean scopable) {
        this.scopable = scopable;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public void setGuidelines(List<String> guidelines) {
        this.guidelines = guidelines;
    }

    public void setAutoOptionSorting(Boolean autoOptionSorting) {
        this.autoOptionSorting = autoOptionSorting;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }
}
