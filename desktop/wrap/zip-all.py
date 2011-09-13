#coding: gbk
from zipfile import ZipFile
zfile = ZipFile('小艺有数.zip', 'w')
zfile.write('Account.db')
zfile.write('小艺有数.exe')
zfile.write('dependencies/jcommon-1.0.16.jar')
zfile.write('dependencies/jfreechart-1.0.13.jar')
zfile.write('dependencies/nachocalendar-0.23.jar')
zfile.write('dependencies/sqlitejdbc-v056.jar')
zfile.write('dependencies/poi-3.7-20101029.jar')
zfile.write('core.jar')
zfile.close()