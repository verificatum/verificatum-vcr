# Verificatum Core Routines (VCR)

## Overview

This package contains the core routines for Verificatum software. It
is modular and well documented to allow easy use and verification. For
comprehensive information and documentation we refer the reader to
https://www.verificatum.org.

For improved efficiency, the most time critical parts can optionally
be linked to routines in:

* The GNU MP library (GMP) and the GMP Modular Exponentiation
  Extension (GMPMEE) library, using Verificatum Multiplicative
  Group library for Java (VMGJ),

* The Verificatum Elliptic Curve library (VEC), using the Verificatum
  Elliptic Curve library for Java (VECJ), which is based on the GNU MP
  library (GMP).

The overhead for making native calls is very small in the context of
modular exponentiations and scalar multiplications in elliptic
curves. Thus, this makes VMN almost as fast as if it was implemented
directly in C while keeping the simplicity of Java code for the
protocol logic.

The following assumes that you are using a release. Developers should
also read `README_DEV`.


## Quick Start

Assuming that you did a standard installation of VMGJ and VECJ you can
simply configure everything, build and install using the following
snippet, but **we strongly advice against this in real applications**
unless it has already been verified to be adequate on your platform
and in your application.

        ./configure --enable-vmgj --enable-vecj
        make
        sudo make install
        vog -rndinit RandomDevice /dev/urandom

We configure the software to use `/dev/urandom` as the random source
which may, or may not, be suitable on your platform. If the pure Java
version is used, then the configure flags are simply dropped.


## Building

1. You need to install Open JDK 10 (or later) and M4.

2. Please use

        ./configure
        make

   to build the software.

3. If you want to use native code for modular exponentiations etc,
   then you must install VMGJ and VECJ first. We refer to the
   installation instructions of these packages.

   Note that for security reasons the versions of these libraries must
   match those stipulated in configure.ac **exactly** and that it does
   not suffice to rename the jar files to pass the configuration
   tests.

   On most platforms the following will work directly

        ./configure --enable-vmgj --enable-vecj

   assuming that you installed VMGJ and VECJ in the standard way. It
   uses the commands `vmgj-<VERSION>-path` and `vecj-<VERSION>-path`
   to set the necessary environment variables unless the environment
   variables `VMGJ_JAR` and `VMGJ_JAR` are set.

   If this does not work, then you can set the environment variables
   `LD_LIBRARY_PATH`, `VMGJ_JAR`, and `VECJ_JAR`. For example:

        LD_LIBRARY_PATH=/usr/local/lib
        VMGJ_JAR=/usr/local/share/java/verificatum-vmgj-<VERSION>.jar
        VECJ_JAR=/usr/local/share/java/verificatum-vecj-<VERSION>.jar


4. Optionally, you may run a few unit tests, by

        make check

   This takes some time, so please be patient.


## Installing

**WARNING! Please read the following instructions carefully. Failure
  to do so may result in a completely insecure installation.**

1. Please use

        make install

   to install the software. You may need to be root or use sudo.

2. The tools in the library, e.g., vog, that require a source of
   randomness to function, uses the random source defined by two files
   that by default are named:

       $HOME/.verificatum_random_source

       $HOME/.verificatum_random_seed

   The first stores a description of a random device or a PRG and the
   second stores a random seed if a PRG is used.

   Here $HOME denotes the home directory of the current user. The
   command vog is a script that invokes the java interpreter on the
   class com.verificatum.ui.gen.GeneratorTool.

   You may override the location of these files by setting the
   environment variables:

       VERIFICATUM_RANDOM_SOURCE
       VERIFICATUM_RANDOM_SEED

   **WARNING!**

   **If an adversary is able to write to any of these files, then the
     software provides no security at all.**

   **If an adversary is able to merely read from the second file, then
     the software provides no security at all, but the contents of the
     first file can safely be made public if it cannot be changed.**

   **If you use the environment variables, then you must make sure
     that nobody can modify them.**

3. The above two files must be initialized using vog before any
   commands that require randomness are used. You can do this as
   follows.

       vog -rndinit RandomDevice <my device>
       Successfully initialized random source!

   If you wish to use a PRG instead, then you need to provide a seed
   as well, e.g., to use a provably secure PRG under the DDH
   assumption you could execute:

       vog -rndinit -seed seedfile PRGElGamal -fixed 2048
       Successfully initialized random source! Deleted seed file.

   The command replaces the seed file each time it is invoked to avoid
   accidental reuse.

   If you wish to change the random source you need to remove the
   files that store the random source and initialize it again with
   your new choice.

   **WARNING!**

   **The provided seed file must contain bits that are
     indistinguishable from truly random bits. The seed bits must not
     be reused here or anywhere else.**

   **Failure to provide a proper seed file may result in a
     catastrophic privacy breach.**


## Usage

Comprehensive documentation ready for printing can be downloaded at
<https://www.verificatum.org>.

## API Documentation

You may use
 
        make api

to invoke Javadoc to build the API. The API is not installed
anywhere. You can copy it to any location.


## Reporting Bugs

Minor bugs should be reported in the repository system as issues or
bugs. Security critical bugs, vulnerabilities, etc should be reported
directly to Verificatum AB. We will make best effort to disclose the
information in a responsible way before the finder gets proper credit.