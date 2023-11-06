package tech.uom.tools.cldr;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import org.junit.jupiter.api.Test;

public class JsonTest {

	@Test
	public void test() {
		//String json = "{name=\"json\",bool:true,int:1,double:2.2,func:function(a){ return a; },array:[1,2]}";
		String json = "[1, 2]";
		//JsonArrayBuilder aBuilder = Json.createArrayBuilder();
		//aBuilder.add("name");
		//JsonArray array = aBuilder.build();
		 JsonReader jsonReader = Json.createReader(new StringReader(json));
		 JsonArray jsonArr = jsonReader.readArray();		 
		 //JsonObject jsonObj = jsonReader.readObject();
		 jsonReader.close();
		//JSONObject jsonObject = new JsonObjectBuilder()..  .fromObject( json );  
		//Object bean = JSONObject.toBean( jsonObject );
		 assertFalse(jsonArr.isEmpty());
		 assertEquals(2, jsonArr.size());
		
//		assertEquals( jsonObject.get( "name" ), PropertyUtils.getProperty( bean, "name" ) );  
//		assertEquals( jsonObject.get( "bool" ), PropertyUtils.getProperty( bean, "bool" ) );  
//		assertEquals( jsonObject.get( "int" ), PropertyUtils.getProperty( bean, "int" ) );  
//		assertEquals( jsonObject.get( "double" ), PropertyUtils.getProperty( bean, "double" ) );  
//		assertEquals( jsonObject.get( "func" ), PropertyUtils.getProperty( bean, "func" ) );
		//List expected = array.getValuesAs(ValueType.class);
		//List expected = JSONArray.toList( jsonObject.getJSONArray( "array" ) );  
//		Assertions.assertListEquals( expected, (List) PropertyUtils.getProperty( bean, "array" ) );  
	}

}
