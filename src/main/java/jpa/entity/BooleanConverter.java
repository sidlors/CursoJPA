//package jpa.entity;
//
//import javax.persistence.AttributeConverter;
//import javax.persistence.Converter;
//
//@Converter
//public class BooleanConverter implements AttributeConverter<Boolean, Integer> {
//
//	public Integer convertToDatabaseColumn(Boolean attribute) {
//		if (Boolean.TRUE.equals(attribute)) {
//			return 1;
//		} else {
//			return -1;
//		}
//	}
//
//	public Boolean convertToEntityAttribute(Integer dbData) {
//		if (dbData == null) {
//			return Boolean.FALSE;
//		} else {
//			if (dbData == 1) {
//				return Boolean.TRUE;
//			} else {
//				return Boolean.FALSE;
//			}
//		}
//	}
//
//}
