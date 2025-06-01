# KP-COMMONS - A library for handling common problems Kaiserpfalz EDV-Service encounters

> You don't need to be crazy to be my friend ... ok, maybe you do. It's just more fun that way.
>
> -- @blue_eyed_darkness on TikTok

[![Release](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/release.yml/badge.svg)](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/release.yml)
[![github-pages](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/github-pages.yml/badge.svg)](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/github-pages.yml)
[![JavaRunner](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/build-and-publish-java-runner-to-quay.yaml/badge.svg)](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/build-and-publish-java-runner-to-quay.yaml)
[![helm](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/publish-helm-webservice.yaml/badge.svg)](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/publish-helm-webservice.yaml)
[![CodeQL](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/codeql-analysis.yml)
[![CI](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/ci.yml/badge.svg)](https://github.com/KaiserpfalzEDV/kp-users/actions/workflows/ci.yml)

## Abstract

This is a library of software artifacts I found useful in a number of projects.
So I assembled them in this library.

## License

The license for the software is GPL 3.0 or newer.
Parts of the software may be licensed under other licences like MIT or Apache 2.0 - these files are marked appropriately.

* _libravatar_ is published under MIT license from Alessandro Leite.

  And of course every single source file with the unlicense as header.
  Pull requests for these parts need to be accompanied by the text

  > I dedicate any and all copyright interest in this software to the
  > public domain. I make this dedication for the benefit of the public at
  > large and to the detriment of my heirs and successors. I intend this
  > dedication to be an overt act of relinquishment in perpetuity of all
  > present and future rights to this software under copyright law.```

  as laid out on https://unlicense.org.

## Architecture

tl;dr (ok, only the bullshit bingo words):

* Immutable Objects (where frameworks allow)
* Relying heavily on generated code
* 100 % test coverage of human generated code
* Every line of code not written is bug free!

Code test coverage for human generated code should be 100%, machine generated code is considered bug free until proven wrong.
Every line that needs not be written is a bug free line without need to test it.
So aim for not writing code.
And yes, I'm struggling with this requirement.
Beat me.

## Included libraries

* kp-users-model
* kp-users-messaging
* kp-users-store
* kp-users-client

## Included microservices

* kp-users-service


## Distribution

The libraries are distributed via maven central. The service itself is distributed as OCI container via quay.io.


## Note from the author

This software is meant do be perfected not finished.

If someone is interested in getting it faster, we may team up.
I'm open for that.
But be warned: I want to _do it right_.
So no shortcuts to get faster.
And be prepared for some basic discussions about the architecture or software design :-).

---
Bensheim, 2025-06-01
