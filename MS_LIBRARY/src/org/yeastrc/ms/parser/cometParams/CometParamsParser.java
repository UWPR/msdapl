/**
 * CometParamsParser.java
 * @author Vagisha Sharma
 * Nov 08, 2013
 * @version 1.0
 */
package org.yeastrc.ms.parser.cometParams;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yeastrc.ms.domain.search.MsTerminalModification;
import org.yeastrc.ms.domain.search.MsTerminalModificationIn;
import org.yeastrc.ms.domain.search.Param;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.domain.search.impl.ResidueModification;
import org.yeastrc.ms.domain.search.impl.TerminalModification;
import org.yeastrc.ms.parser.DataProviderException;
import org.yeastrc.ms.parser.sequestParams.SequestParamsParser;


/**
 * 
 */
public class CometParamsParser extends SequestParamsParser {

    private String searchEnzymeCode;
    private String sampleEnzymeCode;

    static final Pattern paramLinePattern = Pattern.compile("^([\\S&&[^=]]+)\\s*=\\s*([^#]*)\\s*#?(.*)");
    static final Pattern variableModParamPattern = Pattern.compile("^variable_mod(\\d+)$");
    
    public Program getSearchProgram() {
        return Program.COMET;
    }
    
    public String paramsFileName() {
        return "comet.params";
    }
    
    public CometParamsParser() {
    	super();
    }
    
    protected String getEnzymeInfoHeader()
    {
    	return "[COMET_ENZYME_INFO]";
    }
    
    protected void addCustomParam(Param param) throws DataProviderException 
    {
    	// sample enzyme number (actual enzyme information will be parsed later in the file
        if (param.getParamName().equalsIgnoreCase("sample_enzyme_number")) {
            sampleEnzymeCode = param.getParamValue();
        }
        
        else if (param.getParamName().equalsIgnoreCase("search_enzyme_number")) {
            searchEnzymeCode = param.getParamValue();
        }
        else if(param.getParamName().toLowerCase().startsWith("variable_mod"))
        {
        	Matcher m = variableModParamPattern.matcher(param.getParamName());
        	if(!m.matches())
        		throw new DataProviderException("Error getting modification index from parameter name "+param.getParamName());
        	int modIndex = Integer.parseInt(m.group(1));
        	parseDynamicMod(param.getParamValue(), modIndex);
        }
    }

    private void parseDynamicMod(String modString, int modIndex) throws DataProviderException
    {
    	/*
    	 # Up to 9 variable modifications are supported
         # format:  <mass> <residues> <0=variable/else binary> <max_mods_per_peptide> <term_distance> <n/c-term> <required>
         #     e.g. 79.966331 STY 0 3 -1 0 0

         # The second entry is the residue(s) that the modifications are possibly applied to.
         # If more than a single residue is modified by the same mass difference, list them all as a string.
         # Use 'n' for N-terminal modfication and 'c' for C-terminal modification.
         # variable_mod01 = 42.010565 nK 0 3 -1 0 0     ... acetylation mod to lysine and N-terminus of all peptides
         # variable_mod01 = 15.994915 n 0 3 0 0 0     ... oxidation of protein's N-terminus
    	 */
    	final String[] tokens = modString.split("\\s+");
    	if(tokens.length < 4)
    	{
    		throw new DataProviderException(getCurrentLineNumber(), "Error parsing dynamic residue modification string", getCurrentLine());
    	}
    	
    	BigDecimal mass = null;
        try {mass = new BigDecimal((tokens[0]));}
        catch(NumberFormatException e) {throw new DataProviderException(getCurrentLineNumber(), "Error parsing modification mass: "+tokens[0], getCurrentLine(), e);}

        // don't consider modifications with mass-shift of 0;
        if (mass.doubleValue() == 0.0) return; 
        
        // modified residues(s) can only be upper-case characters
        String modChars = tokens[1];
        if (!modCharsPattern.matcher(modChars).matches())
            throw new DataProviderException(getCurrentLineNumber(), "Invalid char(s) for modified residue: "+tokens[1], getCurrentLine());
        
        for (int j = 0; j < modChars.length(); j++) {
            char modChar = modChars.charAt(j);
            if(modChar == 'n' || modChar == 'c')
            {
                MsTerminalModification.Terminal term = MsTerminalModification.Terminal.instance(modChar);

                TerminalModification mod = new TerminalModification();
                mod.setModificationMass(mass);
                mod.setModifiedTerminal(term);
                if(term == MsTerminalModification.Terminal.NTERM)
                    mod.setModificationSymbol(MsTerminalModificationIn.NTERM_MOD_CHAR_SEQUEST);
                else if(term == MsTerminalModification.Terminal.CTERM)
                    mod.setModificationSymbol(MsTerminalModificationIn.CTERM_MOD_CHAR_SEQUEST);

                addDynamicTerminalModification(mod);
            }
            else
            {
                ResidueModification mod = new ResidueModification();
                mod.setModificationMass(mass);
                mod.setModifiedResidue(modChar);
                mod.setModificationSymbol(modSymbols[modIndex - 1]);
                addDynamicResidueModification(mod);
            }
        }
    }
    
    protected Pattern getParamLinePattern()
    {
    	return paramLinePattern;
    }
    
    protected String getSearchEnzymeCode()
    {
    	return this.searchEnzymeCode;
    }

    /**
     * @param args
     * @throws DataProviderException 
     */
    public static void main(String[] args) throws DataProviderException {
        //String paramFile = "resources/comet.params";
        String paramsDir = "resources";
        CometParamsParser parser = new CometParamsParser();
        parser.parseParams("remote.server", paramsDir);
    }
}
