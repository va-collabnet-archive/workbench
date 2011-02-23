CREATE
DEFINER=`root`@`%`
TRIGGER `qa_db`.`qa_case_uuid_trigger`
BEFORE INSERT ON `qa_db`.`qa_case`
FOR EACH ROW
begin 
	if new.case_uid  = '0' then 
		SET new.case_uid = UUID();
	end if;
end//

CREATE
DEFINER=`root`@`%`
TRIGGER `qa_db`.`qa_case_upd_uuid_trigger`
BEFORE UPDATE ON `qa_db`.`qa_case`
FOR EACH ROW
begin 
INSERT INTO `qa_db`.`qa_case_log`
(`case_uid`,
`effective_time`,
`status`,
`rule_uid`,
`database_uid`,
`path_uid`,
`tm_component`,
`detail`,
`assigned_to`,
`disposition_status_uid`,
`disposition_status_date`,
`disposition_status_editor`,
`disposition_reason_uid`,
`disposition_annotation`,
`last_status_change`)
VALUES
(old.case_uid,
 old.effective_time,
 old.status,
 old.rule_uid,
 old.database_uid,
 old.path_uid,
 old.QA_component,
 old.detail,
 old.assigned_to,
 old.disposition_status_uid,
 old.disposition_status_date,
 old.disposition_status_editor,
 old.disposition_reason_uid,
 old.disposition_annotation,
 old.last_status_change);
 end//

-- qa_comment Trigger DDL Statements
CREATE
DEFINER=`root`@`%`
TRIGGER `qa_db`.`qa_comment_uuid_trigger`
BEFORE INSERT ON `qa_db`.`qa_comment`
FOR EACH ROW
begin 
  if new.comment_uid  = '0' then 
    SET new.comment_uid = UUID();
  end if; 
end//

CREATE
DEFINER=`root`@`%`
TRIGGER `qa_db`.`qa_comment_update_trigger`
BEFORE UPDATE ON `qa_db`.`qa_comment`
FOR EACH ROW
begin 
    SET new.effective_time = CURRENT_TIMESTAMP;
end//

-- qa_component Trigger DDL Statements
CREATE
DEFINER=`root`@`%`
TRIGGER `qa_db`.`qa_component_uid_trigger`
BEFORE INSERT ON `qa_db`.`qa_component`
FOR EACH ROW
begin 
  if new.component_uid  = '0' then 
    SET new.component_uid = UUID();
  end if;
end//

-- qa_rule Trigger DDL Statements
CREATE
DEFINER=`root`@`%`
TRIGGER `qa_db`.`qa_rule_update_trigger`
BEFORE UPDATE ON `qa_db`.`qa_rule`
FOR EACH ROW
begin 
    SET new.effective_time = CURRENT_TIMESTAMP;
end//
-- qa_run Trigger DDL Statements
CREATE
DEFINER=`root`@`%`
TRIGGER `qa_db`.`qa_run_uid_trigger`
BEFORE INSERT ON `qa_db`.`qa_run`
FOR EACH ROW
begin 
  if new.run_id  = '0' then 
    SET new.run_id = UUID();
  end if; 
  
end//

