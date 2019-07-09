
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
