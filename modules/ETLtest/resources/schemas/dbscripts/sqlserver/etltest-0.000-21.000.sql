/*
 * Copyright (c) 2017-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
-- These tables/procedures were formerly in the vehicle schema controlled by the simpletest module

CREATE SCHEMA etltest;
GO
CREATE TABLE etltest.source(
  RowId INT IDENTITY(1,1),
  container entityid,
  created DATETIME,
  modified DATETIME,
  id VARCHAR(9),
  name VARCHAR(100),
  TransformRun INT,
  rowversion rowversion,

  CONSTRAINT PK_etlsource PRIMARY KEY (rowid),
  CONSTRAINT AK_etlsource UNIQUE (container,id),
  CONSTRAINT FK_etlsource_container FOREIGN KEY (container) REFERENCES core.containers (entityid)
);


CREATE TABLE etltest.target(
  RowId INT IDENTITY(1,1),
  container entityid,
  created DATETIME,
  modified DATETIME,
  id VARCHAR(9),
  name VARCHAR(100),
  diTransformRunId INT,

  CONSTRAINT PK_etltarget PRIMARY KEY (rowid),
  CONSTRAINT AK_etltarget UNIQUE (container,id),
  CONSTRAINT FK_etltarget_container FOREIGN KEY (container) REFERENCES core.containers (entityid)
);

CREATE TABLE etltest.target2
(
  RowId INT NOT NULL,
  container entityid NOT NULL,
  created DATETIME,
  modified DATETIME,

  id VARCHAR(9),
  name VARCHAR(100),
  diTransformRunId INT NOT NULL,

  CONSTRAINT PK_etltarget2 PRIMARY KEY (rowid, container),
  CONSTRAINT AK_etltarget2 UNIQUE (container,id),
  CONSTRAINT FK_etltarget2_container FOREIGN KEY (container) REFERENCES core.containers (entityid)
);

CREATE TABLE etltest.Transfer
(
  RowId INT NOT NULL,
  TransferStart DATETIME NOT NULL,
	transferComplete DATETIME NULL,
	schemaName NVARCHAR(100) NOT NULL,
	description NVARCHAR(1000) NULL,
	log NVARCHAR(MAX) NULL,
	status NVARCHAR(10) NULL,
	container entityid NULL,
	CONSTRAINT PK_transfer PRIMARY KEY (rowid),
	CONSTRAINT FK_etltransfer_container FOREIGN KEY(container) REFERENCES core.Containers (EntityId)
);

CREATE TABLE etltest.[delete]
(
  RowId INT IDENTITY(1,1),
  container entityid,
  created DATETIME,
  modified DATETIME,

  id VARCHAR(9),
  name VARCHAR(100),
  TransformRun INT,
  rowversion rowversion,
  CONSTRAINT PK_delete PRIMARY KEY (rowid),
  CONSTRAINT AK_delete UNIQUE (container,id),
  CONSTRAINT FK_delete_container FOREIGN KEY (container) REFERENCES core.containers (entityid)
);

CREATE TABLE etltest.x180column_source(
  RowId int IDENTITY(1,1) NOT NULL,
  container entityid NULL,
  created datetime NULL,
  modified datetime NULL,
  field5 INT NULL,
  field6 INT NULL,
  field7 INT NULL,
  field8 INT NULL,
  field9 INT NULL,
  field10 INT NULL,
  field11 INT NULL,
  field12 INT NULL,
  field13 INT NULL,
  field14 INT NULL,
  field15 INT NULL,
  field16 INT NULL,
  field17 INT NULL,
  field18 INT NULL,
  field19 INT NULL,
  field20 INT NULL,
  field21 INT NULL,
  field22 INT NULL,
  field23 INT NULL,
  field24 INT NULL,
  field25 INT NULL,
  field26 INT NULL,
  field27 INT NULL,
  field28 INT NULL,
  field29 INT NULL,
  field30 INT NULL,
  field31 INT NULL,
  field32 INT NULL,
  field33 INT NULL,
  field34 INT NULL,
  field35 INT NULL,
  field36 INT NULL,
  field37 INT NULL,
  field38 INT NULL,
  field39 INT NULL,
  field40 INT NULL,
  field41 INT NULL,
  field42 INT NULL,
  field43 INT NULL,
  field44 INT NULL,
  field45 INT NULL,
  field46 INT NULL,
  field47 INT NULL,
  field48 INT NULL,
  field49 INT NULL,
  field50 INT NULL,
  field51 INT NULL,
  field52 INT NULL,
  field53 INT NULL,
  field54 INT NULL,
  field55 INT NULL,
  field56 INT NULL,
  field57 INT NULL,
  field58 INT NULL,
  field59 INT NULL,
  field60 INT NULL,
  field61 INT NULL,
  field62 INT NULL,
  field63 INT NULL,
  field64 INT NULL,
  field65 INT NULL,
  field66 INT NULL,
  field67 INT NULL,
  field68 INT NULL,
  field69 INT NULL,
  field70 INT NULL,
  field71 INT NULL,
  field72 INT NULL,
  field73 INT NULL,
  field74 INT NULL,
  field75 INT NULL,
  field76 INT NULL,
  field77 INT NULL,
  field78 INT NULL,
  field79 INT NULL,
  field80 INT NULL,
  field81 INT NULL,
  field82 INT NULL,
  field83 INT NULL,
  field84 INT NULL,
  field85 INT NULL,
  field86 INT NULL,
  field87 INT NULL,
  field88 INT NULL,
  field89 INT NULL,
  field90 INT NULL,
  field91 INT NULL,
  field92 INT NULL,
  field93 INT NULL,
  field94 INT NULL,
  field95 INT NULL,
  field96 INT NULL,
  field97 INT NULL,
  field98 INT NULL,
  field99 INT NULL,
  field100 INT NULL,
  field101 INT NULL,
  field102 INT NULL,
  field103 INT NULL,
  field104 INT NULL,
  field105 INT NULL,
  field106 INT NULL,
  field107 INT NULL,
  field108 INT NULL,
  field109 INT NULL,
  field110 INT NULL,
  field111 INT NULL,
  field112 INT NULL,
  field113 INT NULL,
  field114 INT NULL,
  field115 INT NULL,
  field116 INT NULL,
  field117 INT NULL,
  field118 INT NULL,
  field119 INT NULL,
  field120 INT NULL,
  field121 INT NULL,
  field122 INT NULL,
  field123 INT NULL,
  field124 INT NULL,
  field125 INT NULL,
  field126 INT NULL,
  field127 INT NULL,
  field128 INT NULL,
  field129 INT NULL,
  field130 INT NULL,
  field131 INT NULL,
  field132 INT NULL,
  field133 INT NULL,
  field134 INT NULL,
  field135 INT NULL,
  field136 INT NULL,
  field137 INT NULL,
  field138 INT NULL,
  field139 INT NULL,
  field140 INT NULL,
  field141 INT NULL,
  field142 INT NULL,
  field143 INT NULL,
  field144 INT NULL,
  field145 INT NULL,
  field146 INT NULL,
  field147 INT NULL,
  field148 INT NULL,
  field149 INT NULL,
  field150 INT NULL,
  field151 INT NULL,
  field152 INT NULL,
  field153 INT NULL,
  field154 INT NULL,
  field155 INT NULL,
  field156 INT NULL,
  field157 INT NULL,
  field158 INT NULL,
  field159 INT NULL,
  field160 INT NULL,
  field161 INT NULL,
  field162 INT NULL,
  field163 INT NULL,
  field164 INT NULL,
  field165 INT NULL,
  field166 INT NULL,
  field167 INT NULL,
  field168 INT NULL,
  field169 INT NULL,
  field170 INT NULL,
  field171 INT NULL,
  field172 INT NULL,
  field173 INT NULL,
  field174 INT NULL,
  field175 INT NULL,
  field176 INT NULL,
  field177 INT NULL,
  field178 INT NULL,
  field179 INT NULL,
  field180 INT NULL,

  CONSTRAINT PK_x180column_source PRIMARY KEY (RowId),
  CONSTRAINT FK_x180column_source_container FOREIGN KEY (container) REFERENCES core.containers (entityid)
);

CREATE TABLE etltest.x180column_target(
  RowId int NOT NULL,
  container entityid NOT NULL,
  created datetime NULL,
  modified datetime NULL,
  field5 INT NULL,
  field6 INT NULL,
  field7 INT NULL,
  field8 INT NULL,
  field9 INT NULL,
  field10 INT NULL,
  field11 INT NULL,
  field12 INT NULL,
  field13 INT NULL,
  field14 INT NULL,
  field15 INT NULL,
  field16 INT NULL,
  field17 INT NULL,
  field18 INT NULL,
  field19 INT NULL,
  field20 INT NULL,
  field21 INT NULL,
  field22 INT NULL,
  field23 INT NULL,
  field24 INT NULL,
  field25 INT NULL,
  field26 INT NULL,
  field27 INT NULL,
  field28 INT NULL,
  field29 INT NULL,
  field30 INT NULL,
  field31 INT NULL,
  field32 INT NULL,
  field33 INT NULL,
  field34 INT NULL,
  field35 INT NULL,
  field36 INT NULL,
  field37 INT NULL,
  field38 INT NULL,
  field39 INT NULL,
  field40 INT NULL,
  field41 INT NULL,
  field42 INT NULL,
  field43 INT NULL,
  field44 INT NULL,
  field45 INT NULL,
  field46 INT NULL,
  field47 INT NULL,
  field48 INT NULL,
  field49 INT NULL,
  field50 INT NULL,
  field51 INT NULL,
  field52 INT NULL,
  field53 INT NULL,
  field54 INT NULL,
  field55 INT NULL,
  field56 INT NULL,
  field57 INT NULL,
  field58 INT NULL,
  field59 INT NULL,
  field60 INT NULL,
  field61 INT NULL,
  field62 INT NULL,
  field63 INT NULL,
  field64 INT NULL,
  field65 INT NULL,
  field66 INT NULL,
  field67 INT NULL,
  field68 INT NULL,
  field69 INT NULL,
  field70 INT NULL,
  field71 INT NULL,
  field72 INT NULL,
  field73 INT NULL,
  field74 INT NULL,
  field75 INT NULL,
  field76 INT NULL,
  field77 INT NULL,
  field78 INT NULL,
  field79 INT NULL,
  field80 INT NULL,
  field81 INT NULL,
  field82 INT NULL,
  field83 INT NULL,
  field84 INT NULL,
  field85 INT NULL,
  field86 INT NULL,
  field87 INT NULL,
  field88 INT NULL,
  field89 INT NULL,
  field90 INT NULL,
  field91 INT NULL,
  field92 INT NULL,
  field93 INT NULL,
  field94 INT NULL,
  field95 INT NULL,
  field96 INT NULL,
  field97 INT NULL,
  field98 INT NULL,
  field99 INT NULL,
  field100 INT NULL,
  field101 INT NULL,
  field102 INT NULL,
  field103 INT NULL,
  field104 INT NULL,
  field105 INT NULL,
  field106 INT NULL,
  field107 INT NULL,
  field108 INT NULL,
  field109 INT NULL,
  field110 INT NULL,
  field111 INT NULL,
  field112 INT NULL,
  field113 INT NULL,
  field114 INT NULL,
  field115 INT NULL,
  field116 INT NULL,
  field117 INT NULL,
  field118 INT NULL,
  field119 INT NULL,
  field120 INT NULL,
  field121 INT NULL,
  field122 INT NULL,
  field123 INT NULL,
  field124 INT NULL,
  field125 INT NULL,
  field126 INT NULL,
  field127 INT NULL,
  field128 INT NULL,
  field129 INT NULL,
  field130 INT NULL,
  field131 INT NULL,
  field132 INT NULL,
  field133 INT NULL,
  field134 INT NULL,
  field135 INT NULL,
  field136 INT NULL,
  field137 INT NULL,
  field138 INT NULL,
  field139 INT NULL,
  field140 INT NULL,
  field141 INT NULL,
  field142 INT NULL,
  field143 INT NULL,
  field144 INT NULL,
  field145 INT NULL,
  field146 INT NULL,
  field147 INT NULL,
  field148 INT NULL,
  field149 INT NULL,
  field150 INT NULL,
  field151 INT NULL,
  field152 INT NULL,
  field153 INT NULL,
  field154 INT NULL,
  field155 INT NULL,
  field156 INT NULL,
  field157 INT NULL,
  field158 INT NULL,
  field159 INT NULL,
  field160 INT NULL,
  field161 INT NULL,
  field162 INT NULL,
  field163 INT NULL,
  field164 INT NULL,
  field165 INT NULL,
  field166 INT NULL,
  field167 INT NULL,
  field168 INT NULL,
  field169 INT NULL,
  field170 INT NULL,
  field171 INT NULL,
  field172 INT NULL,
  field173 INT NULL,
  field174 INT NULL,
  field175 INT NULL,
  field176 INT NULL,
  field177 INT NULL,
  field178 INT NULL,
  field179 INT NULL,
  field180 INT NULL,

  CONSTRAINT PK_x180column_target PRIMARY KEY (RowId, container),
  CONSTRAINT FK_x180column_target_container FOREIGN KEY (container) REFERENCES core.containers (entityid)
  )
GO

CREATE PROCEDURE etltest.etlTest
	@transformRunId int,
	@containerId entityid = NULL OUTPUT,
	@rowsInserted int = 0 OUTPUT,
	@rowsDeleted int = 0 OUTPUT,
	@rowsModified int = 0 OUTPUT,
	@returnMsg varchar(100) = 'default message' OUTPUT,
	@debug varchar(1000) = '',
	@filterRunId int = null,
	@filterStartTimeStamp datetime = null,
	@filterEndTimeStamp datetime = null,
	@testMode int,
	@testInOutParam varchar(10) = null OUTPUT,
	@runCount int = 1 OUTPUT,
	@previousFilterRunId int = null OUTPUT,
	@previousFilterStartTimeStamp datetime = null OUTPUT,
	@previousFilterEndTimeStamp datetime = null OUTPUT
AS
BEGIN

/*
	Test modes
	1	normal operation
	2	return code > 0
	3	raise error
	4	input/output parameter persistence
	5	override of persisted input/output parameter
	6	Run filter strategy, require @filterRunId. Test persistence.
	7	Modified since filter strategy, require @filterStartTimeStamp & @filterEndTimeStamp. Test persistence.

*/

IF @testMode IS NULL
BEGIN
	SET @returnMsg = 'No testMode set'
	RETURN 1
END

IF @testMode = 1
BEGIN
	print 'Test print statement logging'
	SET @rowsInserted = 1
	SET @rowsDeleted = 2
	SET @rowsModified = 4
	SET @returnMsg = 'Test returnMsg logging'
	RETURN 0
END

IF @testMode = 2 RETURN 1

IF @testMode = 3
BEGIN
	SET @returnMsg = 'Intentional SQL Exception From Inside Proc'
	RAISERROR(@returnMsg, 11, 1)
END

IF @testMode = 4 AND @testInOutParam != 'after' AND @runCount > 1
BEGIN
	SET @returnMsg = 'Expected value "after" for @testInOutParam on run count = ' + convert(varchar, @runCount) + ', but was ' + @testInOutParam
	RETURN 1
END

IF @testMode = 5 AND @testInOutParam != 'before' AND @runCount > 1
BEGIN
	SET @returnMsg = 'Expected value "before" for @testInOutParam on run count = ' + convert(varchar, @runCount) + ', but was ' + @testInOutParam
	RETURN 1
END

IF @testMode = 6
BEGIN
	IF @filterRunId IS NULL
	BEGIN
		SET @returnMsg = 'Required @filterRunId value not supplied'
		RETURN 1
	END
	IF @runCount > 1 AND (@previousFilterRunId IS NULL OR @previousFilterRunId <= @filterRunId)
	BEGIN
		SET @returnMsg = 'Required @filterRunId was not persisted from previous run.'
		RETURN 1
	END
	SET @previousFilterRunId = @filterRunId
END

IF @testMode = 7
BEGIN
	IF @runCount > 1 AND (@previousFilterStartTimeStamp IS NULL OR @previousFilterEndTimeStamp IS NULL
							OR @previousFilterStartTimeStamp <= @filterStartTimeStamp OR @previousFilterEndTimeStamp <= @filterEndTimeStamp)
	BEGIN
		SET @returnMsg = 'Required @filterStartTimeStamp or @filterEndTimeStamp were not persisted from previous run.'
		RETURN 1
	END
	SET @previousFilterStartTimeStamp = @filterStartTimeStamp
	SET @previousFilterEndTimeStamp = @filterEndTimeStamp
END

-- set value for persistence tests
IF @testInOutParam IS NOT NULL SET @testInOutParam = 'after'

RETURN 0

END
GO

EXEC core.fn_dropifexists 'etlTestResultSet', 'etltest', 'PROCEDURE', NULL;
 GO
 CREATE PROCEDURE [etltest].[etlTestResultSet]
 	@transformRunId int,
 	@containerId varchar(100) = NULL OUTPUT,
 	@rowsInserted int = 0 OUTPUT,
 	@rowsDeleted int = 0 OUTPUT,
 	@rowsModified int = 0 OUTPUT,
 	@returnMsg varchar(100) = 'default message' OUTPUT,
 	@debug varchar(1000) = '',
 	@filterRunId int = null,
 	@filterStartTimeStamp datetime = null OUTPUT,
 	@filterEndTimeStamp datetime = null OUTPUT,
 	@testMode int,
 	@testInOutParam varchar(10) = null OUTPUT,
 	@runCount int = 1 OUTPUT,
 	@previousFilterRunId int = null OUTPUT,
 	@previousFilterStartTimeStamp datetime = null OUTPUT,
 	@previousFilterEndTimeStamp datetime = null OUTPUT
 AS
 BEGIN

 IF @testMode = 9
 BEGIN
 	SELECT * FROM etltest.source WHERE container = @containerId
 END

 IF @testInOutParam IS NOT NULL SET @testInOutParam = 'after'

 RETURN 0

 END

 GO

ALTER PROCEDURE etltest.etlTest
	@transformRunId int,
	@containerId entityid = NULL OUTPUT,
	@rowsInserted int = 0 OUTPUT,
	@rowsDeleted int = 0 OUTPUT,
	@rowsModified int = 0 OUTPUT,
	@returnMsg varchar(100) = 'default message' OUTPUT,
	@debug varchar(1000) = '',
	@filterRunId int = null,
	@filterStartTimeStamp datetime = null OUTPUT,
	@filterEndTimeStamp datetime = null OUTPUT,
	@testMode int,
	@testInOutParam varchar(10) = null OUTPUT,
	@runCount int = 1 OUTPUT,
	@previousFilterRunId int = -1 OUTPUT,
	@previousFilterStartTimeStamp datetime = null OUTPUT,
	@previousFilterEndTimeStamp datetime = null OUTPUT
AS
BEGIN

/*
	Test modes
	1	normal operation
	2	return code > 0
	3	raise error
	4	input/output parameter persistence
	5	override of persisted input/output parameter
	6	Run filter strategy, require @filterRunId. Test persistence.
	7	Modified since filter strategy, require @filterStartTimeStamp & @filterEndTimeStamp. Test persistence.

*/

IF @testMode IS NULL
BEGIN
	SET @returnMsg = 'No testMode set'
	RETURN 1
END

IF @runCount IS NULL
    SET @runCount = 1;
  ELSE
    SET @runCount = @runCount + 1;

IF @testMode = 1
BEGIN
	print 'Test print statement logging'
	SET @rowsInserted = 1
	SET @rowsDeleted = 2
	SET @rowsModified = 4
	SET @returnMsg = 'Test returnMsg logging'
	RETURN 0
END

IF @testMode = 2 RETURN 1

IF @testMode = 3
BEGIN
	SET @returnMsg = 'Intentional SQL Exception From Inside Proc'
	RAISERROR(@returnMsg, 11, 1)
END

IF @testMode = 4 AND @testInOutParam != 'after' AND @runCount > 1
BEGIN
	SET @returnMsg = 'Expected value "after" for @testInOutParam on run count = ' + convert(varchar, @runCount) + ', but was ' + @testInOutParam
	RETURN 1
END

IF @testMode = 5 AND @testInOutParam != 'before' AND @runCount > 1
BEGIN
	SET @returnMsg = 'Expected value "before" for @testInOutParam on run count = ' + convert(varchar, @runCount) + ', but was ' + @testInOutParam
	RETURN 1
END

IF @testMode = 6
BEGIN
	IF @filterRunId IS NULL
	BEGIN
		SET @returnMsg = 'Required @filterRunId value not supplied'
		RETURN 1
	END
	IF @runCount > 1 AND (@previousFilterRunId IS NULL OR @previousFilterRunId >= @filterRunId)
	BEGIN
		SET @returnMsg = 'Required @filterRunId was not persisted from previous run.'
		RETURN 1
	END
	SET @previousFilterRunId = @filterRunId
END

IF @testMode = 7
  BEGIN
    IF @runCount > 1 AND (@filterStartTimeStamp IS NULL AND @filterEndTimeStamp IS NULL)
    BEGIN
      SET @returnMsg = 'Required filterStartTimeStamp or filterEndTimeStamp were not persisted from previous run.';
      RETURN 1;
    END;
    SET @filterStartTimeStamp = CURRENT_TIMESTAMP;
    SET @filterEndTimeStamp = CURRENT_TIMESTAMP;
  END;

IF @testMode = 8

  BEGIN
    IF @runCount > 1 AND ((@previousFilterStartTimeStamp IS NULL AND @previousFilterEndTimeStamp IS NULL)
                         OR (@filterStartTimeStamp IS NULL AND @filterEndTimeStamp IS NULL))
    BEGIN
      SET @returnMsg = 'Required filterStartTimeStamp or filterEndTimeStamp were not persisted from previous run.';
      RETURN 1;
    END;
    SET @previousFilterStartTimeStamp = coalesce(@filterStartTimeStamp, CURRENT_TIMESTAMP);
    SET @previousFilterEndTimeStamp = coalesce(@filterEndTimeStamp, CURRENT_TIMESTAMP);
  END;

-- set value for persistence tests
IF @testInOutParam IS NOT NULL AND @testInOutParam != '' SET @testInOutParam = 'after'

RETURN 0

END
GO