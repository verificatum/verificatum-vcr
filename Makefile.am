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

.PHONY: clean cleanfindbugs cleancheckstyle cleanpmd cleananalysis cleanapi check runtests

ACLOCAL_AMFLAGS=-I m4

##################### Variables used by Automake ############################

jardir = $(datadir)/java

# libdir is defined by Autoconf.

########################## Custom variables #################################

CWD = $(shell pwd)
SRC = src
PKGPATH = $(SRC)/java
MFSRC = $(SRC)/mf
APIROOT = api
CLASSESROOT = classes
JARFILE = $(PACKAGE)$(VMGJ_POSTFIX)$(VECJ_POSTFIX)-$(VERSION).jar
VCR_JAR = $(jardir)/$(JARFILE)
ALL_JAR = $(VCR_JAR):$(VMGJ_JAR):$(VECJ_JAR)
CLASSPATH := .:$(CLASSESROOT):$(ALL_JAR)
BINDIR = bin
SRC_BINDIR = $(SRC)/bin
INSTA = tools/installation

# Code analysis.
STATANA = tools/staticanalysis
CSDIR = $(STATANA)/checkstyle

FBDIR = $(STATANA)/findbugs
FBPKGPATH = $(FBDIR)/java
FBCLASSES = $(FBDIR)/classes

PMDDIR = $(STATANA)/pmd
PMDPKGPATH = $(PMDDIR)/java
PMDCLASSES = $(PMDDIR)/classes

COVERAGE = $(CWD)/tools/coverage
JCOVDIR = $(COVERAGE)/jcov
COBDIR = $(COVERAGE)/cobertura

TMP_TEST_DIR = .tmp_test_dir

# Complete version including all sublibraries.
COMPLETE_VERSION=$(shell ./tools/installation/complete_version $(VERSION) "$(VMGJ_COMPLETE_VERSION)" "$(VECJ_COMPLETE_VERSION)")

# Variables pointing to external resources.
FBCLASSPATH = /usr/share/java/findbugs-annotations-3.0.1.jar
JCOVJARDIR = $(HOME)/jcov_2.0
COBHOME = $(HOME)/cobertura-2.1.1
COBCLASSPATH=$(shell find $(COBHOME) | grep "\.jar" | grep -E "cobertura.*/cobertura-[^-]*\.jar|asm-|slf4j-|logback-|oro-"| tr "\n" ":")
JDK_DOC="http://docs.oracle.com/javase/8/docs/api"

# Although this is a pure Java library, VECJ and VMGJ are not, so we
# need to forward their library paths to our wrapper scripts.
export LD_LIBRARY_PATH := $(LD_LIBRARY_PATH_VMGJ):$(LD_LIBRARY_PATH_VECJ):$(libdir)

# Choose wether to use VMGJ or not.
if VMGJ
   USE_VMGJ = USE_VMGJ
   VMGJGREP = --invert-match MillerRabin
else
   USE_VMGJ = USE_PURE_JAVA
   VMGJGREP = .
endif

######################## Extracting targets  ################################

# Files created by preprocessing magic-files.
PREPROCESSED_JAVANAMES = $(shell find $(PKGPATH) -name "*.magic" \
| tr "." "/" | sed -e "s£/magic£\.java£g" \
| grep $(VMGJGREP) | sort | uniq | tr "\n" " ")

# Space separated list of all Java source filenames (both existing and
# those to be generated).
FILES_JAVANAMES = $(PREPROCESSED_JAVANAMES) \
$(shell find $(PKGPATH) -name "*.java" \
| grep $(VMGJGREP) | sort | uniq | tr "\n" " ")

# Resource files that must be copied as is.
FILES_TXTNAMES = $(shell find $(PKGPATH) -name "*.txt" \
| sed -e s£$(PKGPATH)£$(CLASSESROOT)£g | tr "\n" " ")

# Executable wrapper scripts.
files_script = $(shell find $(SRC_BINDIR) -name "*.src" \
| grep -v vcr-info \
| sed -e s£\\.src££g | sed -e s£$(SRC_BINDIR)£$(BINDIR)£| tr "\n" " ")

# Space separated list of all test classes implementing unit tests.
TEST_CLASSNAMES=`cat test_classnames | tr "\n" " " | sed "s/\s\s*/ /"`

# Space separated list of all test classes implementing unit tests.
excerr_classnames = $(shell find $(PKGPATH) -name "*.java" -o -name "*.magic" \
| grep -E "Exception|Error" | sed -e s£$(PKGPATH)££g \
| sed -e s£\\.java££g | sed -e s£/£\\.£g | sort | uniq | tr "\n" " " \
| sed -e "s/\s*$$//")

############################## Compiling ####################################

all: jar scripts

# Generate Jar manifest with current version.
$(CLASSESROOT)/MANIFEST_NM.MF: $(MFSRC)/MANIFEST.MF.src
	mkdir -p $(@D)
	cat .macros.m4 $(MFSRC)/MANIFEST.MF.src | m4 > $(CLASSESROOT)/MANIFEST_NM.MF

# Pre-processing rule for generating .java-files from .magic-files.
%.java : %.magic preprocessor.m4
	cat preprocessor.m4 $< | m4 > $@

# Makefile-rule for copying resource files of classes to the right
# location.
$(CLASSESROOT)/%.txt : $(PKGPATH)/%.txt
	mkdir -p $(@D)
	cp $< $(@D)

# Compile all Java files.
.classes.stamp: $(FILES_TXTNAMES) $(FILES_JAVANAMES)
	mkdir -p $(CLASSESROOT)
	$(JAVAC) -version
	$(JAVAC) -Xlint:unchecked -classpath $(CLASSPATH) -d $(CLASSESROOT) $(AM_JAVACFLAGS) $(JAVACFLAGS) $(FILES_JAVANAMES)
	@touch .classes.stamp

# Build JAR file. The designers of JAR have decided to change the
# manifest, so we need to make a copy to avoid a spurious dependency.
jar : $(JARFILE)
$(JARFILE): .classes.stamp $(CLASSESROOT)/MANIFEST_NM.MF
	cp $(CLASSESROOT)/MANIFEST_NM.MF $(CLASSESROOT)/MANIFEST.MF
	cd $(CLASSESROOT); $(JAR) cfm $@ MANIFEST.MF com; cp $@ ..

# Additional flags for the JVM. Historically this was used because the
# implementors of JVM failed to handle 32-bit and 64-bit native
# binaries correctly. They claim to handle it correctly now.
#
# if BITS64
#    JVM_FLAGS=-d64 # 64-bit machine
# else
#    JVM_FLAGS=-d32 # 32-bit machine
# endif
#
# Amount of memory used by the JVM. Our software execute in less than
# 2GB, so this is conservative.
# JVM_FLAGS=-Xms1024m -Xmx6000m
JVM_FLAGS=

# We generate M4 macros based on make/environment variables to be able
# to generate our wrapper script with hardcoded paths to jars and
# paths, and other values.
scriptmacros.m4: preprocessor.m4
	@printf "changequote([[[[[,]]]]])dnl\n" > scriptmacros.m4
	@printf "define(M4_JVM_FLAGS, $(JVM_FLAGS))dnl\n" >> scriptmacros.m4
	@printf "define(M4_VERSION, $(VERSION))dnl\n" >> scriptmacros.m4
	@printf "define(M4_COMPLETE_VERSION, $(COMPLETE_VERSION))dnl\n" >> scriptmacros.m4
	@printf "define(M4_BINDIR, $(bindir))dnl\n" >> scriptmacros.m4
	@printf "define(M4_LIBDIR, $(LD_LIBRARY_PATH))dnl\n" >> scriptmacros.m4
	@printf "define(M4_ALL_JAR, $(ALL_JAR))dnl\n" >> scriptmacros.m4
	@printf "define(M4_VERIFICATUM_VOG_BUILTIN, $(BCVOG))dnl\n" >> scriptmacros.m4
	@printf "define(M4_JAVA_FLAGS, [[[[[$(JAVA_FLAGS)]]]]])" >> scriptmacros.m4

# Create the destination directory.
$(BINDIR) :
	mkdir -p $(BINDIR)

scripts: $(files_script)
$(files_script): $(BINDIR)/%: $(BINDIR) $(SRC_BINDIR)/%.src scriptmacros.m4
	cat scriptmacros.m4 $(SRC_BINDIR)/$*.src | m4 > $@
	chmod +x $@

$(BINDIR)/vcr-$(VERSION)-info: $(BINDIR) scriptmacros.m4 $(SRC_BINDIR)/vcr-info.src
	mkdir -p $(BINDIR)
	cat scriptmacros.m4 $(SRC_BINDIR)/vcr-info.src | m4 > $(BINDIR)/vcr-$(VERSION)-info
	chmod +x $(BINDIR)/vcr-$(VERSION)-info


########################## Static analysis ##################################

# Edit the wrappers in the subdirectories of $(STATANA) if needed.

# Do a Checkstyle analysis of the source and save the result in
# $(CSDIR)/checkstyle_report.txt.
checkstyle: $(CSDIR)/checkstyle_report.txt $(CSDIR)/checkstyle_filter.sh
$(CSDIR)/checkstyle_report.txt: $(JARFILE)
	rm -f $(CSDIR)/checkstyle_report_tmp.txt
	cd $(CSDIR); ./checkstyle_wrapper checkstyle_configure.xml checkstyle_raw_report.txt $(CWD)/$(PKGPATH); ./checkstyle_filter.sh checkstyle_raw_report.txt checkstyle_report.txt

# Do a Findbugs analysis of the jar and save the result in
# $(FBDIR)/findbugs_report.txt. Edit the wrapper if needed.
#
# All annotations are commented out in the code to avoid dependencies
# to $(FBCLASSPATH) simply to do code analysis, i.e.,
# @SuppressFBWarnings(...) appears as // FB_ANNOTATION
# @SuppressFBWarnings(...). Only when the code is analyzed are these
# exposed. This is why we need to process the source code before
# analysis.

FBFILES_JAVANAMES = $(shell echo $(FILES_JAVANAMES) \
                      | sed "s|$(PKGPATH)|$(FBPKGPATH)|g")
FBFILES_TXTNAMES = $(shell echo $(FILES_TXTNAMES) \
                     | sed "s|$(PKGPATH)|$(FBCLASSES)|g")

$(FBPKGPATH)/%.java : $(PKGPATH)/%.java
	mkdir -p $(@D)
	cat $< | sed "s|// FB_ANNOTATION||" > $@
$(FBCLASSES)/%.txt : $(PKGPATH)/%.txt
	mkdir -p $(@D)
	cp $< $(@D)
$(FBCLASSES) :
	mkdir -p $@
$(FBCLASSES)/MANIFEST.MF : $(CLASSESROOT)/MANIFEST.MF
	mkdir -p $(@D)
	cp $< $(@D)

fbjar: $(FBPKGPATH)/$(JARFILE)
$(FBPKGPATH)/$(JARFILE): $(FBCLASSES)/MANIFEST.MF $(FBCLASSES) $(FBFILES_JAVANAMES) $(FBFILES_TXTNAMES)
	$(JAVAC) -Xlint:unchecked -classpath $(CLASSPATH):$(FBCLASSPATH) -d $(FBCLASSES) $(AM_JAVACFLAGS) $(JAVACFLAGS) $(FBFILES_JAVANAMES)
	cd $(FBCLASSES); $(JAR) cfm $(JARFILE) MANIFEST.MF com; cp $(JARFILE) ..

findbugs: $(FBDIR)/findbugs_report.txt
$(FBDIR)/findbugs_report.txt: $(FBPKGPATH)/$(JARFILE)
	./$(FBDIR)/findbugs_wrapper -textui -exclude $(FBDIR)/findbugs_configure.xml -auxclasspath $(FBCLASSPATH) $(FBDIR)/$(JARFILE) > $(FBDIR)/findbugs_report.txt


# Do a PMD analysis of the source and save the result in
# $(PMDDIR)/pmd_report.txt. Edit the wrapper if needed.
#
# All annotations are commented out in the code to avoid dependencies
# simply to do code analysis, i.e., @SuppressFBWarnings(...) appears
# as // PMD_ANNOTATION @SuppressWarnings(...). Only when the code is
# analyzed are these exposed. This is why we need to process the
# source code before analysis.
#
# Additionally, PMD allows to use the tag // NOPMD to suppress the
# errors from a given line.
PMDFILES_JAVANAMES = $(shell echo $(FILES_JAVANAMES) \
                      | sed "s|$(PKGPATH)|$(PMDPKGPATH)|g")
PMDFILES_TXTNAMES = $(shell echo $(FILES_TXTNAMES) \
                     | sed "s|$(PKGPATH)|$(PMDPKGPATH)|g")

$(PMDPKGPATH)/%.java : $(PKGPATH)/%.java
	mkdir -p $(@D)
	cat $< | sed "s|// PMD_ANNOTATION||" > $@
$(PMDPKGPATH)/%.txt : $(PKGPATH)/%.txt
	mkdir -p $(@D)
	cp $< $(@D)

pmd: $(PMDDIR)/pmd_report.txt
$(PMDDIR)/pmd_report.txt: $(PMDFILES_JAVANAMES) $(PMDFILES_TXTNAMES)
	./$(PMDDIR)/pmd_wrapper -rulesets $(PMDDIR)/pmd_ruleset.xml -d $(PMDPKGPATH) | sed "s/.*verificatum\/\(com\/verificatum.*\)/\1/p" | uniq > $(PMDDIR)/pmd_raw_report.txt
	cd $(PMDDIR); ./pmd_filter.sh pmd_raw_report.txt pmd_report.txt


# Execute all static analyzers.
analysis: checkstyle findbugs pmd $(STATANA)/generate_analysis.sh
	cd $(STATANA); ./generate_analysis.sh
	cat $(STATANA)/analysis_report.txt

# Build api.
api: $(JARFILE)
	javadoc -classpath $(CLASSPATH) -d $(APIROOT) -link $(JDK_DOC) $(FILES_JAVANAMES)


########################## Coverage analysis ################################


JCOV_EXCLUSIONS=-e com.verificatum.arithm.MillerRabinTrial \
-e com.verificatum.test.Test \
-e com.verificatum.test.TestParameters \
-e $(shell echo $(TEST_CLASSNAMES) $(excerr_classnames) \
     | sed -e "s/\ /\ -e\ /g" | tr "\f" "\n")

jcov: $(JARFILE) $(files_script)
	mkdir -p $(JCOVDIR)
	cd $(JCOVDIR); rm -f filtered.xml
	cp $(JARFILE) $(JCOVDIR)/
	cd $(JCOVDIR); java -jar $(JCOVJARDIR)/jcov.jar Instr -t template.xml $(JCOV_EXCLUSIONS) -field on -abstract off $(JARFILE)
	cd $(JCOVDIR); CLASSPATH=$(JCOVJARDIR)/jcov_file_saver.jar:$(JARFILE) ../../../$(BINDIR)/vtest -exec $(TEST_CLASSNAMES)
	cd $(JCOVDIR); java -jar $(JCOVJARDIR)/jcov.jar RepGen $(JCOV_EXCLUSIONS) result.xml

# Set COBHOME above to use this. A recent version is required. After
# downloading Cobertura you need to convert the .sh files to Unix
# format with dos2unix and make them executable before running.

COBERTURA_CLASSES=$(shell find $(PKGPATH) -name "*.java" \
| grep -E "arithm|eio|util|crypto" \
| grep -v -E "com/verificatum/arithm/MillerRabinTrial|com/verificatum/test|.*/Test.*|Exception|Error" \
| sed "s/\.java/\.class/g" \
| sed "s|$(PKGPATH)|$(CLASSESROOT)|")

cobertura: $(JARFILE) $(files_script)
	rm -rf $(COBDIR)
	rm -f cobertura.ser
	mkdir -p $(COBDIR)/classes
	$(COBHOME)/cobertura-instrument.sh \
--auxClasspath classes \
--ignoreMethodAnnotation com.verificatum.annotation.CoberturaIgnore \
--destination $(COBDIR)/classes $(COBERTURA_CLASSES)
	CLASSPATH=$(COBCLASSPATH):$(COBDIR)/classes:$(CLASSESROOT) java \
-Djava.security.egd=file:/dev/./urandom \
-Djavax.accessibility.assistive_technologies=java.lang.String \
-ea \
com.verificatum.test.Test vtest -exec \
$(TEST_CLASSNAMES)
	$(COBHOME)/cobertura-report.sh --srcdir $(PKGPATH) --destination $(COBDIR)/report --format html $(COBDIR)/classes

coverage: jcov cobertura


########################### Usage Targets ###################################

# The names of test classes starts with "Test" (typically followed by
# the class that is tested). The command line tool for executing the
# tests implemented in a given test class is "vtest".
#
# We are fully aware of the testing frameworks available and have made
# a deliberate decision to not use them. We view static analysis and
# coverage analysis as development tools, whereas tests are an
# integral part of the software that should be completely
# self-contained.
#
# If there is any problem when "make check" is executed, then the
# command line tools can be used to debug a single test
# separately. Use the "-h" option to learn about the options accepted
# by the tools.

check: runtests

# Verifying non-interactive classes.
runtests: $(BINDIR)/vtest .classes.stamp
	@rm -rf $(TMP_TEST_DIR)
	@mkdir -p $(TMP_TEST_DIR)
	LD_LIBRARY_PATH=$(LD_LIBRARY_PATH) CLASSPATH=$(CLASSPATH) \
$(BINDIR)/vtest -exec $(TEST_CLASSNAMES)
	@rm -rf $(TMP_TEST_DIR)


######################### Installation ######################################

install-data-hook:
	@echo ""
	@echo "================================================================"
	@echo ""
	@echo "             ADDITIONAL STEPS AND DOCUMENTATION"
	@echo ""
	@echo " To avoid accidental use of old version of the software our"
	@echo " commands hardcode specific jar files used both the one"
	@echo " generated by this package and those of other packages."
	@echo ""
	@echo " Use \"vcr-$(VERSION)-info complete\" to print the complete "
	@echo " version including all libraries used."
	@echo ""
	@echo " Before you can use the software you need to initialize the "
	@echo " random source. Read more by executing \"vog -h\" or simply use "
	@echo " the standard secure source of randomness in Un*x:"
	@echo ""
	@echo "     $$ vog -rndinit RandomDevice /dev/urandom"
	@echo ""
	@echo " Please consult the README.md file before using the software."
	@echo " There is also comprehensive documentation in the form of"
	@echo " white papers at https://www.verificatum.org."
	@echo ""
	@echo "================================================================"
	@echo ""

dist_noinst_DATA = README.md COPYING NEWS AUTHORS ChangeLog $(PKGPATH) .version.m4 .macros.m4 tools $(SRC)

jar_DATA = $(JARFILE)
dist_bin = $(files_script) $(BINDIR)/vcr-$(VERSION)-info
dist_bin_SCRIPTS = $(files_script) $(BINDIR)/vcr-$(VERSION)-info

############################# Cleaning ######################################

cleanapi:
	rm -rf $(APIROOT)

cleancheckstyle:
	rm -rf $(CSDIR)/checkstyle_raw_report.txt $(CSDIR)/checkstyle_report.txt

cleanfindbugs:
	rm -rf $(FBDIR)/findbugs_report.txt $(FBPKGPATH) $(FBCLASSES) $(FBDIR)/$(JARFILE)

cleanpmd:
	rm -rf $(PMDDIR)/pmd_report.txt $(PMDDIR)/pmd_raw_report.txt $(PMDPKGPATH)

cleananalysis: cleancheckstyle cleanfindbugs cleanpmd
	rm -rf $(STATANA)/analysis_report.txt

cleanjcov:
	rm -rf $(JCOVDIR)

cleancobertura:
	rm -rf $(COBDIR) cobertura.ser

cleancoverage: cleanjcov cleancobertura

clean-local: cleanapi cleananalysis cleancoverage
	find . -name "*~" -delete
	@rm -rf verificatum-vcr*.tar $(INSTA)/*.class $(files_script) scriptmacros.m4 $(JARFILE) $(TMP_TEST_DIR) $(TMP_DEMO_DIR) $(PREPROCESSED_JAVANAMES) $(APIROOT) $(CLASSESROOT) *.stamp .*.stamp compile $(BINDIR)/vcr-$(VERSION)-info
