package com.github.forax.jayspec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

class SerializationDecoder {
  private static final byte TC_NULL = 0x70;
  private static final byte TC_REFERENCE = 0x71;
  private static final byte TC_CLASSDESC = 0x72;
  private static final byte TC_OBJECT = 0x73;
  private static final byte TC_STRING = 0x74;
  private static final byte TC_ARRAY = 0x75;
  private static final byte TC_CLASS = 0x76;
  private static final byte TC_BLOCKDATA = 0x77;
  private static final byte TC_ENDBLOCKDATA = 0x78;
  private static final byte TC_RESET = 0x79;
  private static final byte TC_BLOCKDATALONG = 0x7A;
  private static final byte TC_EXCEPTION = 0x7B;
  private static final byte TC_LONGSTRING = 0x7C;
  private static final byte TC_PROXYCLASSDESC = 0x7D;
  private static final byte TC_ENUM = 0x7E;
  
  private static final int BASE_HANDLE = 0x7e0000;
  
  static class Type {
    final String name;
    SerializationDecoder.Type superType;
    final String[] fields;
    
    Type(String name, String[] fields) {
      this.name = name;
      this.fields = fields;
    }
  }
  
  private final ArrayList<Object> handleList =
      new ArrayList<>();
  
  public Object decode(ByteBuffer buffer) {
    buffer.getInt();  // magic + version number
    return content(buffer);
  }
  
  private Object content(ByteBuffer buffer) {
    byte kind = buffer.get();
    switch(kind) {
    case TC_NULL:   
      return null;
    case TC_REFERENCE: {
      int handleIndex = buffer.getInt() - BASE_HANDLE;
      return handleList.get(handleIndex);
    }
    case TC_CLASSDESC: {
      return classDescInfo(buffer);
    }
    case TC_OBJECT: 
      return newObject(buffer); 
    case TC_STRING: {
      String string = readUTF(buffer);
      newHandle(string);
      return string;
    }
    case TC_ARRAY:
      return newArray(buffer);
    case TC_CLASS:   
      return newClass(buffer);
      
    case TC_BLOCKDATA:   
    case TC_ENDBLOCKDATA: 
    case TC_RESET:   
    case TC_BLOCKDATALONG:   
    case TC_EXCEPTION:   
    case TC_LONGSTRING:
    case TC_PROXYCLASSDESC:   
    case TC_ENUM:  
    default:
      throw new AssertionError("content kind " + Integer.toHexString(kind));
    }
  }
  
  private HashMap<String, Object> newObject(ByteBuffer buffer) {
    String[] fields = ((SerializationDecoder.Type)content(buffer)).fields;
    HashMap<String, Object> map = new HashMap<>();
    newHandle(map);
    for(int i = 0; i < fields.length; i++) {
      String field = fields[i];
      Object value = readValue(field.charAt(0), buffer);
      map.put(field.substring(1), value);
    }
    return map;
  }
  
  private Object[] newArray(ByteBuffer buffer) {
    SerializationDecoder.Type type = (SerializationDecoder.Type)content(buffer);
    int size = buffer.getInt();
    Object[] array = new Object[size];
    newHandle(array);
    // only works with an array of objects
    for(int i = 0; i < array.length; i++) {
      array[i] = content(buffer); 
    }
    return array;
  }
  
  private SerializationDecoder.Type newClass(ByteBuffer buffer) {
    SerializationDecoder.Type type = (SerializationDecoder.Type)content(buffer);
    newHandle(type);
    return type;
  }
  
  private void newHandle(Object object) {
    handleList.add(object);
  }
  
  private Object readValue(char typeCode, ByteBuffer buffer) {
    switch(typeCode) {
    case 'Z': // boolean
    case 'B': // byte
      return buffer.get();
    case 'C': // char
      return buffer.getChar();
    case 'D': // double
      return buffer.getDouble();
    case 'F': // float
      return buffer.getFloat();
    case 'I': // integer
      return buffer.getInt();
    case 'J': // long
      return buffer.getLong();
    case 'S':// short
      return buffer.getShort();
    default: // array or object
      return content(buffer);
    }
  }
  
  private SerializationDecoder.Type classDescInfo(ByteBuffer buffer) {
    String className = readUTF(buffer);
    long serialUID = buffer.getLong();
    byte classDescFlags = buffer.get();
    int fieldCount = buffer.getShort() /*& 0xFFFF*/;
    String[] fields = new String[fieldCount];
    SerializationDecoder.Type type = new Type(className, fields);
    newHandle(type);
    for(int i = 0; i < fieldCount; i++) {
      char typeCode = (char)buffer.get();
      String fieldName = readUTF(buffer);
      if (typeCode == 'L' || typeCode == '[') {
        content(buffer);  // class name
      }
      fields[i] = typeCode + fieldName;
    }
    int classAnnotation = buffer.get();
    if (classAnnotation != 0x78) { //ENDBLOCKDATA
      throw new AssertionError();
    }
    content(buffer); // superclass
    return type;
  }
  
  private static String readUTF(ByteBuffer buffer) {
    int length = (buffer.get() & 0xFF) << 8 | (buffer.get() & 0xFF);
    char[] array = new char[length];

    int i = 0;
    int current;
    for(;;) {  // fastpath
        if (i == length) {
          return new String(array);
        }
        if ((current =  buffer.get() & 0xFF) > 127) {
          break;
        }
        array[i++] = (char)current;
    }

    int count = i;
    while (i < length) {
        switch (current >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:  // 0xxxxxxx
                array[count++] = (char)current;
                i++;
                break;
            case 12: case 13: { // 110xxxxx 10xxxxxx
                i += 2;
                int current2 = buffer.get();
                array[count++]=(char)(((current & 0x1F) << 6) | (current2 & 0x3F));
                break;
            }
            default: {          // 1110xxxx 10xxxxxx 10xxxxxx
                i += 3;
                int current2 = buffer.get();
                int current3 = buffer.get();
                array[count++]=(char)(((current & 0x0F) << 12) | ((current2 & 0x3F) << 6)  | ((current3 & 0x3F) << 0));
                break;
            }
        }
        current = buffer.get();
    }
    return new String(array, 0, count);
  }
}