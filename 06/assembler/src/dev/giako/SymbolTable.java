package dev.giako;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a correspondence between symbolic labels and numeric addresses.
 */
public class SymbolTable {
    private final Map<String, Integer> symbols = new HashMap<>();

    /**
     * Adds the pair (symbol, address) to the table.
     * @param symbol the symbol to add
     * @param address the related address
     */
    public void addEntry(String symbol, int address) {
        symbols.put(symbol, address);
    }

    /**
     * Does the symbol table contain the given symbol?
     * @param symbol the symbol string
     * @return true if the table contains the symbol
     */
    public boolean symbol(String symbol) {
        return symbols.containsKey(symbol);
    }

    /**
     * Returns the address associated with the symbol
     * @param symbol the symbol string
     * @return the address related to the symbol
     */
    public int getAddress(String symbol) {
        return symbols.get(symbol);
    }


}
