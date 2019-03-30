title: http4k blog: 
description: 

# 

##### [@daviddenton][github] 

## Intro

### Lenses - a recap
In [http4k], Lenses are typically used to provide typesafe conversion of typed values into and out of 
HTTP messages, although this concept has been extended within the [http4k] ecosystem to support that of a 
form handling and request contexts.

A Lens is an stateless object responsible for either the one-way (or Bidirectional) transformation of data.
It defines type parameters representing input `IN` and output `OUT` types and implements one (for a `Lens`) 
or both (for a `BiDiLens`) of the following interfaces:

1. **LensExtractor** - takes a value of type `IN` and extracts a value of type `OUT`
2. **LensInjector** - takes a value of type `IN` and a value of type `OUT` and returns a modified value of 
type `IN` with the value injected into it.

<script src="https://gist-it.appspot.com/https://github.com/http4k/http4k/blob/master/src/docs/blog/http4k_lenses/lens_definition.kt"></script>

The creation of a Lens consists of 4 main concerns:

1. **targeting** determines where the Lens expects to extract and inject the values from/to, which can 
consist of both an overall target and a name within that target.
2. **multiplicity** handling which defines how many of a particular value we are attempting to handle.
3. the **transformation** chain of function composition which forms a specification for converting one 
type to another. This is done in code using the `map()` method defined on the Lens.
4. the **optionality** of a Lens denotes the behaviour if/when a value cannot be found in the target.

To define a Lens instance through the [http4k] Lens API, we take an initial **target** specification, decide 
it's **multiplicity**, provide any **transformations** with `map()`, and finally reify the specification 
into a Lens instance by deciding it's optionality.

It sounds involved, but it is consistent and the fluent API has been designed to make it simpler. By way 
of an example, here we define a bi-directional Lens for custom type `Page`, extracted from a querystring 
value and defaulting to Page 1.

<script src="https://gist-it.appspot.com/https://github.com/http4k/http4k/blob/master/src/docs/blog/http4k_lenses/lens_example.kt"></script>

In [http4k], Lenses are typically used to provide typesafe conversion of typed values into and out of HTTP 
messages, although this concept has been extended within the [http4k] ecosystem to support that of a form 
handling and request contexts.

[github]: http://github.com/daviddenton
