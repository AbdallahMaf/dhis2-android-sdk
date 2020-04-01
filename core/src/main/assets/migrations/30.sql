ALTER TABLE DataElement RENAME TO DataElement_Old;
CREATE TABLE DataElement (_id INTEGER PRIMARY KEY AUTOINCREMENT, uid TEXT NOT NULL UNIQUE, code TEXT, name TEXT, displayName TEXT, created TEXT, lastUpdated TEXT, shortName TEXT, displayShortName TEXT, description TEXT, displayDescription TEXT, valueType TEXT, zeroIsSignificant INTEGER, aggregationType TEXT, formName TEXT, numberType TEXT, domainType TEXT, dimension TEXT, displayFormName TEXT, optionSet TEXT, categoryCombo TEXT NOT NULL, FOREIGN KEY (optionSet) REFERENCES OptionSet (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (categoryCombo) REFERENCES CategoryCombo (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED);
INSERT INTO DataElement (_id, uid, code, name, displayName, created, lastUpdated, shortName, displayShortName, description, displayDescription, valueType, zeroIsSignificant, aggregationType, formName, numberType, domainType, dimension, displayFormName, optionSet, categoryCombo) SELECT _id, uid, code, name, displayName, created, lastUpdated, shortName, displayShortName, description, displayDescription, valueType, zeroIsSignificant, aggregationType, formName, numberType, domainType, dimension, displayFormName, optionSet, categoryCombo FROM DataElement_Old;
DROP TABLE DataElement_Old;