/**
 * Implementazione DAO su file system: persiste studenti e passaggi in file CSV nella root del
 * progetto, con caching in memoria sincronizzato sui timestamp dei file e lock espliciti per la
 * concorrenza in scrittura.
 */
package com.ispw.uniride.dao.fs;
