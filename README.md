# Mustache

This is another _Java_ implementation of the [Mustache](http://mustache.github.com/) templating language. Mustache focuses on text rendering with the least possible coupling to the business logic, ensuring a good separation of concerns.

## Features

This implementation is v1.1.2 compliant but doesn't support lambdas (maybe with java 8 ?). It's written with separation of concerns in mind and offers the following features :

* standalone serializable _processors_
* a parser API for _processor_ creation from a template file
* a rendering API to merge a _processor_ with actual _data_
* the Mustache class facility you can extend

On top of it, it has a nice integration to Spring Web MVC and shows fairly good performance.

## Usage

The easiest way is to extend the _Mustache_ class. It has out-of-the-box API to render a template as shown in the "chris" unit test :

```java
public class ChrisTest {

  String template = "Hello {{name}}\nYou have just won ${{value}}!\n{{#in_ca}}\nWell, ${{taxed_value}}, after taxes.\n{{/in_ca}}\n";
  String expected = "Hello Chris\nYou have just won $10000!\nWell, $6000.0, after taxes.\n";
  
  @Test
  public void shouldRenderAsExcpected() throws ParseException, IOException {
    
    Mustache chrisExample = new Mustache() {
      String name = "Chris";
      int value = 10000;
      float taxed_value() {
        return value - (value * .4F);
      }
      boolean in_ca = true;
    };
    
    StringBuilder result = new StringBuilder();
    chrisExample.renderString(template, result, null);
    
    Assert.assertEquals(expected, result.toString());
  }
  
}
```

