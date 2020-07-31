CREATE TABLE DataSetCompulsoryDataElementOperandsLink (_id INTEGER PRIMARY KEY AUTOINCREMENT, dataSet TEXT NOT NULL, dataElementOperand TEXT NOT NULL, FOREIGN KEY (dataSet) REFERENCES DataSet (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (dataElementOperand) REFERENCES DataElementOperand (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, UNIQUE (dataSet, dataElementOperand));