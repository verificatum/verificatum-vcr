
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
# 2 Variable name for complete version.
# 3 Variable name for library path.
# 4 Variable name for path to jar file.
# 5 Name of library info command.
# 6 Name of jar file for informational purposes.
# 7 Name of class to test for loading.
# 8 Expected version in jar file manifest.
AC_DEFUN([ACE_MODULE],[

AC_ARG_ENABLE([$2],
     [  --enable-$2    Use the $2 library.],
     [case "${enableval}" in
       yes) $2=true ;

            # If no info command is given explicitly, then we assume
            # that it has been installed.
            if test x$6 = x;
            then
                AC_SUBST([$2_INFO_COMMAND],$2-$9-info)
            else
                AC_SUBST([$2_INFO_COMMAND],$6)
            fi

            # Make sure that the info command exists.
            if ! ( command -v ${$2_INFO_COMMAND} > /dev/null 2>&1 );
            then
                AC_MSG_ERROR([No info command ${$2_INFO_COMMAND} can be found!])
            fi

            # Extract complete version.
            AC_SUBST([$3],`${$2_INFO_COMMAND} complete`)

            # Extract library path.
            AC_SUBST([$4],`${$2_INFO_COMMAND} lib`)

	    export LD_LIBRARY_PATH=$[$4]

            # Extract jar file and check that it can be loaded and has
            # the right version in its manifest.
            AC_SUBST([$5],`${$2_INFO_COMMAND} jar`)
            ACE_CHECK_LOADJAR([$2],[${$5}],[$7],[$8],[$9])

            ;;

       no)  $2=false ;;
      esac],[$2=false])
AM_CONDITIONAL([$1], [test x$$2 = xtrue])

])
