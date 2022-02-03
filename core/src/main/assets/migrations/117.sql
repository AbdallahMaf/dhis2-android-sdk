# Add IndicatorLegendSetLink table (ANDROSDK-1469)

CREATE TABLE IndicatorLegendSetLink(_id INTEGER PRIMARY KEY AUTOINCREMENT, indicator TEXT NOT NULL, legendSet TEXT NOT NULL, FOREIGN KEY (indicator) REFERENCES Indicator (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (legendSet) REFERENCES LegendSet (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, UNIQUE (indicator, legendSet));
