
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

# 1 Name of flag for configure.
# 2 Path to jar file.
# 3 Name of jar file for informational purposes.
# 4 Name of class to test for loading.
# 5 Expected version in jar file manifest.

AC_DEFUN([ACE_CHECK_LOADJAR],[
AC_REQUIRE([ACE_PROG_JAVA])

AC_ARG_ENABLE([check-$1],
     [  --disable-check-$1    Skip checking that $3 is installed.],
     [],[
ace_res=$($JAVA $JAVAFLAGS -classpath $2:tools/installation TestLoadJar $4 $5)

echo -n "checking for $3... "
if test "x$ace_res" = x;
then
   echo "yes"
else
   echo "no"
   AC_MSG_ERROR([$ace_res

Please make sure that $3 and its native libraries are installed.
])
fi
])
])
