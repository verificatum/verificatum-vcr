
# Copyright 2008-2019 Douglas Wikstrom
#
# This file is part of Verificatum Core Routines (VCR).
#
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation
# files (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use, copy,
# modify, merge, publish, distribute, sublicense, and/or sell copies
# of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
# BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
# ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
y
AC_PREREQ([2.71])
AC_INIT([verificatum-vcr],[m4_esyscmd([m4 .version.m4])],[info@verificatum.com])
AC_CANONICAL_TARGET # Must appear before AM_INIT_AUTOMAKE
AM_INIT_AUTOMAKE([foreign tar-ustar -Wall -Werror -Wno-portability])
AC_CONFIG_MACRO_DIR([m4])

# We require specific versions of the VMGJ and VECJ packages for
# security reasons. The version is verified both in terms of the name
# of the jar file and the contents of the jar manifest file, so
# renaming files will fail.
VMGJ_VERSION=1.2.2
VECJ_VERSION=2.1.5

# Checks for development tools we need.
ACE_PROG_JAVAC
ACE_PROG_JAR
ACE_PROG_JAVADOC
ACE_PROG_JAVA

cd tools/installation
javac TestLoadJar.java
cd -

AC_CONFIG_FILES([Makefile])

# Check that the right version of VMGJ is installed if enabled.
ACE_MODULE([VMGJ],
           [vmgj],
           [VMGJ_COMPLETE_VERSION],
           [VMGJ_LD_LIBRARY_PATH],
           [VMGJ_JAR],
           [${VMGJ_INFO}],
           [verificatum-vmgj-${VMGJ_VERSION}.jar],
           [com.verificatum.vmgj.VMG],
           [${VMGJ_VERSION}])

# Check that the right version of VECJ is installed if enabled.
ACE_MODULE([VECJ],
           [vecj],
           [VECJ_COMPLETE_VERSION],
           [VECJ_LD_LIBRARY_PATH],
           [VECJ_JAR],
           [${VECJ_INFO}],
           [verificatum-vecj-${VECJ_VERSION}.jar],
           [com.verificatum.vecj.VEC],
           [${VECJ_VERSION}])

# Build macro for preprocessing based on the configuration. This is
# done in such a way that if configure changes any parameters that
# have an effect on the preprocessing, then the preprocessing script
# is rebuilt. This then triggers preprocessed files to be rebuilt,
# since preprocessed files depend on preprocessor.m4 in Makefile.am.

printf "" > preprocessor_new.m4

if test x$vmgj = xtrue;
then
   printf ["define(\`USE_VMGJ')"] >> preprocessor_new.m4

   # Avoid m4 to puke on the string dnl
   printf "dn" >> preprocessor_new.m4
   printf "l\n" >> preprocessor_new.m4

   AC_SUBST([VMGJ_POSTFIX],[-vmgj])
fi

if test x$vecj = xtrue;
then
   printf ["define(\`USE_VECJ')"] >> preprocessor_new.m4

   # Avoid m4 to puke on the string dnl
   printf "dn" >> preprocessor_new.m4
   printf "l\n" >> preprocessor_new.m4

   AC_SUBST([VECJ_POSTFIX],[-vecj])
fi

cat src/m4/preprocessing.m4 >> preprocessor_new.m4
if test ! -f preprocessor.m4
then
   touch preprocessor.m4
fi
if test ! "`diff preprocessor_new.m4 preprocessor.m4`"x = x
then
   mv preprocessor_new.m4 preprocessor.m4
fi
rm -f preprocessor_new.m4

AC_OUTPUT
