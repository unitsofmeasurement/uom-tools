/*
 * Units of Measurement Tools for Java
 * Copyright (c) 2015-2023, Werner Keil and others.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of JSR-363 nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tech.uom.tools.cldr;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;

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
