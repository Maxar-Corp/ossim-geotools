package org.ossim.core

class Tile
{
    String  epsgCode
    Long    id
    Double  minx
    Double  miny
    Double  maxx
    Double  maxy
    Integer w
    Integer h
    Integer level
    Integer row
    Integer col
    Integer globalRow
    Integer globalCol
    def files
    def entries
    def parentTiles

    String toString()
    {
       [epsgCode:epsgCode, 
        id:id, 
        minx:minx, 
        miny:miny, 
        maxx:maxx, 
        maxy:maxy,
        w:w, 
        h:h, 
        level:level, 
        row:row, 
        col:col,
        globalRow:globalRow,
        globalCol:globalCol] 
    }
}