
# Copyright 2008-2018 Douglas Wikstrom
#
# This file is part of Verificatum Core Routines (VCR).
#
# VCR is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# VCR is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General
# Public License for more details.
#
# You should have received a copy of the GNU Affero General Public
# License along with VCR. If not, see <http://www.gnu.org/licenses/>.

AC_PREREQ([2.63])
AC_INIT([verificatum-vcr],
        [m4_esyscmd([m4 .version.m4])],
        [info@verificatum.com])
AC_CANONICAL_SYSTEM # Must appear before AM_INIT_AUTOMAKE
AM_INIT_AUTOMAKE([tar-ustar -Wall -Werror -Wno-portability])
AC_CONFIG_MACRO_DIR([m4])

# We require specific versions of the VMGJ and VECJ packages for
# security reasons. The version is verified both in terms of the name
# of the jar file and the contents of the jar manifest file, so
# renaming files will fail.
VMGJ_VERSION=1.2.1
VECJ_VERSION=2.1.4

# Checks for development tools we need.
ACE_PROG_JAVAC
ACE_PROG_JAVAH
ACE_PROG_JAR
ACE_PROG_JAVADOC
ACE_PROG_JAVA

cd tools/installation
javac TestLoadJar.java
cd -

# We need to figure out if we need to pass -J-d64 to javac.
AM_CONDITIONAL([BITS64], [test x`getconf LONG_BIT` = x64])

AC_CONFIG_FILES([Makefile])

AM_CONDITIONAL([BITS64], [test x`getconf LONG_BIT` = x64])


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
