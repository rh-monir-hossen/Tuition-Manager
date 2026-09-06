export interface EntityField {
  name: string;
  type: string;
  isPk?: boolean;
  isFk?: boolean;
  fkTarget?: string;
  isNullable?: boolean;
  defaultVal?: string;
  isIndex?: boolean;
  note?: string;
}

export interface DatabaseEntity {
  id: string;
  name: string;
  tableName: string;
  category: 'Core' | 'Schedule' | 'Finance' | 'Sync & System';
  description: string;
  badge?: string;
  fields: EntityField[];
  deleteRule?: string;
  indexes?: string[];
}

export interface SheetsTab {
  tabName: string;
  purpose: string;
  columns: string[];
}

export interface DevelopmentPhase {
  number: number;
  title: string;
  shortTitle: string;
  objective: string;
  filesAffected: string[];
  dependencies: string[];
  databaseChanges: string;
  featuresCompleted: string[];
  testingRequirements: string;
  exitCriteria: string;
  category: 'Core' | 'Features' | 'Finance' | 'Sync' | 'Release';
}

export interface ArchitectureLayer {
  name: string;
  color: string;
  textColor: string;
  tech: string;
  scope: string;
  details: string[];
}
