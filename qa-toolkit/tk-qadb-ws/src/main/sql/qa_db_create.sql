CREATE DATABASE  IF NOT EXISTS `qa_db` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `qa_db`;
-- MySQL dump 10.13  Distrib 5.1.34, for apple-darwin9.5.0 (i386)
--
-- Host: localhost    Database: qa_db
-- ------------------------------------------------------
-- Server version	5.0.77

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--

--
-- Table structure for table `qa_run_rules`
--

DROP TABLE IF EXISTS `qa_run_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_run_rules` (
  `rule_uid` varchar(36) NOT NULL,
  `run_id` varchar(36) NOT NULL,
  PRIMARY KEY  (`rule_uid`,`run_id`),
  KEY `FK_QA_Run` (`run_id`),
  KEY `fk_rr_rule` (`rule_uid`),
  KEY `fk_rr_run` (`run_id`),
  CONSTRAINT `fk_rr_rule` FOREIGN KEY (`rule_uid`) REFERENCES `qa_rule` (`rule_uid`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_rr_run` FOREIGN KEY (`run_id`) REFERENCES `qa_run` (`run_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_run`
--

DROP TABLE IF EXISTS `qa_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_run` (
  `run_id` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `database_uid` varchar(36) NOT NULL,
  `path_uid` varchar(36) NOT NULL,
  `viewpoint_time` datetime NOT NULL default '1000-01-01 00:00:00',
  `context_name` varchar(150) default NULL,
  `name` varchar(254) default NULL,
  `context_configuration` text,
  `executed_rules_detail` text,
  `run_configuration` text,
  `outcome_details` text,
  `outcome_uid` varchar(36) default NULL,
  `start_time` datetime default NULL,
  `end_time` datetime default NULL,
  `path_name` varchar(1024) default NULL,
  PRIMARY KEY  (`run_id`),
  KEY `fk_run_database` (`database_uid`),
  CONSTRAINT `fk_run_database` FOREIGN KEY (`database_uid`) REFERENCES `qa_database` (`database_uid`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_policy_log`
--

DROP TABLE IF EXISTS `qa_policy_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_policy_log` (
  `policy_uid` varchar(36) NOT NULL,
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `title` varchar(1024) NOT NULL,
  `body` text NOT NULL,
  `author` varchar(120) default NULL,
  `QA_policy_category` varchar(36) default NULL,
  `DITA_documentation_link_uid` varchar(36) default NULL,
  `DITA_generated_topic_uid` varchar(36) default NULL,
  `policy_code` varchar(50) default NULL,
  PRIMARY KEY  (`policy_uid`,`effective_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_comment`
--

DROP TABLE IF EXISTS `qa_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_comment` (
  `comment_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) default '0',
  `comment` text,
  `author` varchar(120) default NULL,
  `case_uid` varchar(36) default NULL,
  PRIMARY KEY  (`comment_uid`),
  KEY `FK_QA_Case` (`case_uid`),
  KEY `fk_case_comment` (`case_uid`),
  CONSTRAINT `fk_case_comment` FOREIGN KEY (`case_uid`) REFERENCES `qa_case` (`case_uid`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `qa_rule_imp`
--

DROP TABLE IF EXISTS `qa_rule_imp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_rule_imp` (
  `rule_uid` varchar(36) NOT NULL,
  `name` text,
  `description` text,
  `severity_uid` varchar(36) default NULL,
  `package_name` varchar(500) default NULL,
  `package_url` varchar(500) default NULL,
  `DITA_documentation_link_UID` varchar(36) default NULL,
  `rule_code` varchar(50) default NULL,
  PRIMARY KEY  (`rule_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `qa_case_log`
--

DROP TABLE IF EXISTS `qa_case_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_case_log` (
  `case_uid` varchar(36) NOT NULL,
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `rule_uid` varchar(36) NOT NULL,
  `database_uid` varchar(36) NOT NULL,
  `path_uid` varchar(36) NOT NULL,
  `tm_component` varchar(36) NOT NULL,
  `detail` text,
  `assigned_to` varchar(120) default NULL,
  `disposition_status_uid` varchar(36) default NULL,
  `disposition_status_date` date default NULL,
  `disposition_status_editor` varchar(120) default NULL,
  `disposition_reason_uid` varchar(36) default NULL,
  `disposition_annotation` varchar(4000) default NULL,
  `last_status_change` timestamp NULL default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qa_case_log`
--

--
-- Table structure for table `qa_rule_log`
--

DROP TABLE IF EXISTS `qa_rule_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_rule_log` (
  `rule_uid` varchar(36) NOT NULL,
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `package_url` varchar(500) default NULL,
  `package_name` varchar(500) default NULL,
  `rule_category` varchar(36) default NULL,
  `severity_uid` varchar(36) default NULL,
  `name` varchar(1000) default NULL,
  `description` text,
  `expected_result` text,
  `suggested_resolution` text,
  `is_Whitelist_allowed` tinyint(4) default NULL,
  `is_Whitelist_reset_allowed` tinyint(4) default NULL,
  `is_Whitelist_reset_when_closed` tinyint(10) default NULL,
  `DITA_documentation_UID` varchar(36) default NULL,
  `DITA_topic_uid` varchar(36) default NULL,
  `modifed_by` varchar(150) default NULL,
  `QA_severity` varchar(36) default NULL,
  `example` text,
  `standing_issues` text,
  `rule_code` varchar(50) default NULL,
  PRIMARY KEY  (`rule_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qa_rule_log`
--

--
-- Table structure for table `qa_case`
--

DROP TABLE IF EXISTS `qa_case`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_case` (
  `case_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `rule_uid` varchar(36) NOT NULL,
  `database_uid` varchar(36) NOT NULL,
  `path_uid` varchar(36) NOT NULL,
  `QA_component` varchar(36) NOT NULL,
  `detail` text,
  `assigned_to` varchar(120) default NULL,
  `disposition_status_uid` varchar(36) default NULL,
  `disposition_status_date` timestamp NULL default NULL,
  `disposition_status_editor` varchar(120) default NULL,
  `disposition_reason_uid` varchar(36) default NULL,
  `disposition_annotation` varchar(4000) default NULL,
  `last_status_change` timestamp NULL default NULL,
  `assignment_editor` varchar(45) default NULL,
  `assignment_date` timestamp NULL default NULL,
  PRIMARY KEY  (`case_uid`),
  KEY `FK_QA_Rule` (`rule_uid`),
  KEY `FK_QA_component` (`QA_component`),
  KEY `FK_QA_disposition_status` (`disposition_status_uid`),
  KEY `fk_case_component` (`QA_component`),
  KEY `fk_case_disposition_status` (`disposition_status_uid`),
  KEY `fk_case_rule` (`rule_uid`),
  KEY `fk_case_database` (`database_uid`),
  CONSTRAINT `fk_case_component` FOREIGN KEY (`QA_component`) REFERENCES `qa_component` (`component_uid`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_case_database` FOREIGN KEY (`database_uid`) REFERENCES `qa_database` (`database_uid`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_case_disposition_status` FOREIGN KEY (`disposition_status_uid`) REFERENCES `qa_disposition_status` (`disposition_status_uid`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_case_rule` FOREIGN KEY (`rule_uid`) REFERENCES `qa_rule` (`rule_uid`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qa_case`
--


--
-- Table structure for table `qa_category`
--

DROP TABLE IF EXISTS `qa_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_category` (
  `category_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `name` varchar(254) NOT NULL,
  `description` varchar(1024) default NULL,
  `author` varchar(120) default NULL,
  PRIMARY KEY  (`category_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `qa_policy`
--

DROP TABLE IF EXISTS `qa_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_policy` (
  `policy_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `title` varchar(1024) NOT NULL,
  `body` text NOT NULL,
  `QA_policy_category` varchar(36) default NULL,
  `author` varchar(120) default NULL,
  `DITA_generated_topic_uid` varchar(36) default NULL,
  `DITA_documentation_link_uid` varchar(36) default NULL,
  `policy_code` varchar(50) default NULL,
  PRIMARY KEY  (`policy_uid`),
  KEY `FK_QA_policy_category` (`QA_policy_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_policy_category`
--

DROP TABLE IF EXISTS `qa_policy_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_policy_category` (
  `policy_category_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `name` varchar(254) NOT NULL,
  `description` varchar(1024) default NULL,
  `author` varchar(120) NOT NULL,
  PRIMARY KEY  (`policy_category_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_finding`
--

DROP TABLE IF EXISTS `qa_finding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_finding` (
  `finding_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `database_uid` varchar(36) NOT NULL,
  `path_uid` varchar(36) NOT NULL,
  `run_id` varchar(36) NOT NULL,
  `rule_uid` varchar(36) NOT NULL,
  `QA_component_uid` varchar(36) default NULL,
  `detail` text,
  `QA_component_name` varchar(1024) default NULL,
  PRIMARY KEY  (`finding_uid`),
  KEY `FK_QA_Run` (`run_id`),
  KEY `fk_finding_rule` (`rule_uid`),
  KEY `fk_finding_run_rules` (`run_id`),
  KEY `FK_QA_component` (`QA_component_uid`),
  CONSTRAINT `fk_finding_run_rules` FOREIGN KEY (`run_id`) REFERENCES `qa_run` (`run_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SCTID_IDENTIFIER`
--

DROP TABLE IF EXISTS `SCTID_IDENTIFIER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCTID_IDENTIFIER` (
  `PARTITION_ID` char(2) default NULL,
  `NAMESPACE_ID` char(7) default NULL,
  `ARTIFACT_ID` varchar(18) default NULL,
  `RELEASE_ID` varchar(8) default NULL,
  `ITEM_ID` int(11) default NULL,
  `SCTID` varchar(18) default NULL,
  `CODE` varchar(512) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_component`
--

DROP TABLE IF EXISTS `qa_component`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_component` (
  `component_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `SCTID` varchar(20) default NULL,
  `name` varchar(1024) default NULL,
  PRIMARY KEY  (`component_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- WARNING: old server version. The following dump may be incomplete.
--

--
-- Table structure for table `qa_rule`
--

DROP TABLE IF EXISTS `qa_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_rule` (
  `rule_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `package_url` varchar(500) default NULL,
  `package_name` varchar(500) default NULL,
  `rule_category` varchar(36) default NULL,
  `severity_uid` varchar(36) default NULL,
  `name` text,
  `description` text,
  `expected_result` text,
  `suggested_resolution` text,
  `example` text,
  `standing_issues` text,
  `is_Whitelist_allowed` tinyint(4) default NULL,
  `is_Whitelist_reset_allowed` tinyint(4) default NULL,
  `is_Whitelist_reset_when_closed` tinyint(10) default NULL,
  `DITA_documentation_link_UID` varchar(36) default NULL,
  `DITA_generated_topic_uid` varchar(36) default NULL,
  `modifed_by` varchar(150) default NULL,
  `rule_code` varchar(50) default NULL,
  `documentation_url` varchar(1000) default NULL,
  PRIMARY KEY  (`rule_uid`),
  KEY `rule_severity_fk` (`severity_uid`),
  KEY `fk_rule_severity` (`severity_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_database`
--

DROP TABLE IF EXISTS `qa_database`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_database` (
  `database_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `name` varchar(1024) NOT NULL,
  `description` varchar(1024) default NULL,
  PRIMARY KEY  (`database_uid`,`effective_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_policy_rule`
--

DROP TABLE IF EXISTS `qa_policy_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_policy_rule` (
  `policy_rule_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `author` varchar(120) default NULL,
  `QA_policy` varchar(36) default NULL,
  `rule_uid` varchar(36) default NULL,
  PRIMARY KEY  (`policy_rule_uid`),
  KEY `FK_QA_policy` (`QA_policy`),
  KEY `FK_QA_Rule` (`rule_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_disposition_status`
--

DROP TABLE IF EXISTS `qa_disposition_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_disposition_status` (
  `disposition_status_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `name` varchar(254) NOT NULL,
  `description` varchar(1024) default NULL,
  `author` varchar(120) default NULL,
  PRIMARY KEY  (`disposition_status_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qa_severity`
--

DROP TABLE IF EXISTS `qa_severity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qa_severity` (
  `severity_uid` varchar(36) NOT NULL default '0',
  `effective_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `status` int(4) NOT NULL default '0',
  `name` varchar(254) NOT NULL,
  `description` varchar(1024) default NULL,
  `author` varchar(120) default NULL,
  PRIMARY KEY  (`severity_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
