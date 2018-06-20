package br.com.leandrobortoli.buildermaster;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuilderMaster {

	Set<String> imports;
	
	public BuilderMaster() {
		imports = new HashSet<String>();
		imports.add("import java.util.Arrays;");
	}

	@SuppressWarnings("rawtypes")
	public void generateBuilderCode(Class clazz) {
		
		String className = clazz.getSimpleName() + "Builder";
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("public class ").append(className).append(" {\n");
		builder.append("\tprivate ").append(clazz.getSimpleName()).append(" element;\n");

		builder.append("\tprivate ").append(className).append("(){}\n\n");

		char firstCharacter = clazz.getSimpleName().toLowerCase().charAt(0);
		boolean vowel = firstCharacter == 'a'
				|| firstCharacter == 'e'
				|| firstCharacter == 'i'
				|| firstCharacter == 'o'
				|| firstCharacter == 'u'
				|| firstCharacter == 'h';
		String article = vowel ? "an" : "a";
		
		builder.append("\tpublic static ").append(className).append(" ").append(article).append(clazz.getSimpleName()).append("() {\n");
		builder.append("\t\t").append(className).append(" builder = new ").append(className).append("();\n");
		builder.append("\t\tinit(builder);\n");
		builder.append("\t\treturn builder;\n");
		builder.append("\t}\n\n");

		builder.append("\tpublic static void init(").append(className).append(" builder) {\n");
		builder.append("\t\tbuilder.element = new ").append(clazz.getSimpleName()).append("();\n");
		builder.append("\t\t").append(clazz.getSimpleName()).append(" element = builder.element;\n");
		builder.append("\n\t\t\n");
		
		List<Field> declaredFields = getClassFields(clazz);
		for(Field field: declaredFields) {
			if(field.getName().equals("serialVersionUID"))
				continue;
			if(Modifier.isStatic(field.getModifiers()))
				continue;
			builder.append("\t\telement.set").append(field.getName().substring(0, 1).toUpperCase()).append(field.getName().substring(1)).append("(").append(getDefaultParameter(field)).append(");\n");
				
		}
		builder.append("\t}\n\n");		
		
		for(Field field: declaredFields) {
			if(field.getName().equals("serialVersionUID"))
				continue;
			if(Modifier.isStatic(field.getModifiers()))
				continue;
			if(field.getType().getSimpleName().equals("List")) {
				ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
				builder.append("\tpublic ")
					.append(className)
					.append(" with").append(field.getName().substring(0, 1).toUpperCase()).append(field.getName().substring(1))
					.append("(").append(((Class)stringListType.getActualTypeArguments()[0]).getSimpleName()).append("... params) {\n");
				builder.append("\t\telement.set").append(field.getName().substring(0, 1).toUpperCase()).append(field.getName().substring(1)).append("(Arrays.asList(params));\n");
				
				builder.append("\t\treturn this;\n");
				builder.append("\t}\n\n");
			} else {
				builder.append("\tpublic ")
					.append(className)
					.append(" with").append(field.getName().substring(0, 1).toUpperCase()).append(field.getName().substring(1))
					.append("(").append(field.getType().getSimpleName()).append(" param) {\n");
				registerImports(field.getType().getCanonicalName());
				builder.append("\t\telement.set")
					.append(field.getName().substring(0, 1).toUpperCase()).append(field.getName().substring(1))
					.append("(param);\n");
				builder.append("\t\treturn this;\n");
				builder.append("\t}\n\n");
			}
		}

		builder.append("\tpublic ").append(clazz.getSimpleName()).append(" build() {\n");
		builder.append("\t\treturn element;\n");
		builder.append("\t}\n");

		builder.append("}");
		
		for(String str: imports) {
			System.out.println(str);
		}
		System.out.println("import " + clazz.getCanonicalName() + ";");
		System.out.println("\n");
		System.out.println(builder.toString());
	}
	
	@SuppressWarnings("rawtypes")
	public List<Field> getClassFields(Class clazz) {
		List<Field> fields = new ArrayList<Field>(); 
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		Class superClass = clazz.getSuperclass();
		if(superClass != Object.class) {
			List<Field> fieldsSC = Arrays.asList(superClass.getDeclaredFields()); 
			fields.addAll(fieldsSC);
		}
		return fields;
	}
	
	public String getDefaultParameter(Field field) {
		String type = field.getType().getSimpleName();
		if(type.equals("int") || type.equals("Integer")){
			return "0";
		}
		if(type.equals("long") || type.equals("Long")){
			return "0L";
		}
		if(type.equals("double") || type.equals("Double")){
			return "0.0";
		}
		if(type.equals("boolean") || type.equals("Boolean")){
			return "false";
		}
		if(type.equals("String")){
			return "\"\"";
		}
		return "null";
	}
	
	public void registerImports(String clazz) {
		if(clazz.contains("."))
			imports.add("import " + clazz + ";");
	}
	
}
