ALTER TABLE DataSetDataElementLink ADD COLUMN categoryCombo TEXT REFERENCES CategoryCombo (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;