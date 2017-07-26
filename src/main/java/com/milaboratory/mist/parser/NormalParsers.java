package com.milaboratory.mist.parser;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.ParserUtils.*;

/**
 * Parsers for objects and their parameters for normal syntax.
 */
final class NormalParsers {
    private final PatternAligner patternAligner;
    private final String query;
    private final List<BracketsPair> squareBracketsPairs;
    private final ArrayList<Integer> startStickMarkers;
    private final ArrayList<Integer> endStickMarkers;
    private final ArrayList<ScoreThreshold> scoreThresholds;
    private final List<BorderFilterBracesPair> borderFilterBracesPairs;
    private final List<NormalSyntaxGroupName> groupNames;

    NormalParsers(PatternAligner patternAligner, String query, List<BracketsPair> squareBracketsPairs,
                  ArrayList<Integer> startStickMarkers, ArrayList<Integer> endStickMarkers,
                  ArrayList<ScoreThreshold> scoreThresholds, List<BorderFilterBracesPair> borderFilterBracesPairs,
                  List<NormalSyntaxGroupName> groupNames) {
        this.patternAligner = patternAligner;
        this.query = query;
        this.squareBracketsPairs = squareBracketsPairs;
        this.startStickMarkers = startStickMarkers;
        this.endStickMarkers = endStickMarkers;
        this.scoreThresholds = scoreThresholds;
        this.borderFilterBracesPairs = borderFilterBracesPairs;
        this.groupNames = groupNames;
    }

    ArrayList<FoundToken> parseRepeatPatterns(List<BracketsPair> repeatPatternBracesPairs) throws ParserException {
        // must be changed to Integer.MAX_VALUE when bitap wrapper for longer sequences will be implemented
        final int MAX_REPEATS = 63;

        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        for (BracketsPair bracesPair : repeatPatternBracesPairs) {
            if (bracesPair.start == 0)
                throw new ParserException("Found '{' without nucleotide in the start of query!");
            String arguments = query.substring(bracesPair.start + 1, bracesPair.end);
            NucleotideSequence patternSeq = toNSeq(query.substring(bracesPair.start - 1, bracesPair.start));
            int minRepeats = 1;
            int maxRepeats = MAX_REPEATS;
            int fixedLeftBorder = startStick(bracesPair.start - 1) ? 0 : -1;
            int fixedRightBorder = endStick(bracesPair.end) ? -2 : -1;
            if (arguments.length() == 0)
                throw new ParserException("Missing number of repeats in " + query.substring(bracesPair.start - 1,
                        bracesPair.end + 1));
            else if (arguments.equals("*"))
                minRepeats = MAX_REPEATS;
            else if (!arguments.contains(":"))
                minRepeats = toInt(arguments, "number of repeats");
            else {
                if (arguments.indexOf(":") != 0)
                    minRepeats = toInt(arguments.substring(0, arguments.indexOf(":")),
                            "minimum number of repeats");
                if (arguments.indexOf(":") != arguments.length() - 1)
                    maxRepeats = toInt(arguments.substring(arguments.indexOf(":") + 1),
                            "maximum number of repeats");
            }
            ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
            ArrayList<FoundGroupName> groupNamesLeft = findGroupNames(bracesPair.start - 1, true);
            ArrayList<FoundGroupName> groupNamesRight = findGroupNames(bracesPair.end, false);
            groupEdgePositions.addAll(groupNamesLeft.stream()
                    .map(gn -> new GroupEdgePosition(new GroupEdge(gn.name, true), 0))
                    .collect(Collectors.toList()));
            groupEdgePositions.addAll(groupNamesRight.stream()
                    .map(gn -> new GroupEdgePosition(new GroupEdge(gn.name, false), MAX_REPEATS))
                    .collect(Collectors.toList()));
            int tokenStart = groupNamesLeft.stream().mapToInt(fgn -> fgn.edgeCoordinate).min()
                    .orElse(bracesPair.start - 1);
            int tokenEnd = groupNamesRight.stream().mapToInt(fgn -> fgn.edgeCoordinate).max()
                    .orElse(bracesPair.end) + 1;

            foundTokens.add(new FoundToken(new RepeatPattern(getPatternAligner(bracesPair.start - 1, bracesPair.end + 1),
                    patternSeq, minRepeats, maxRepeats, fixedLeftBorder, fixedRightBorder, groupEdgePositions),
                    tokenStart, tokenEnd));
        }

        return foundTokens;
    }

    ArrayList<FoundToken> parseFuzzyMatchPatterns(TokenizedString tokenizedString) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        List<Token> stringTokens = tokenizedString.getTokens(0, tokenizedString.getFullLength()).stream()
                .filter(Token::isString).collect(Collectors.toList());
        for (Token currentStringToken : stringTokens) {
            Matcher regexMatcher = Pattern.compile("[a-zA-Z]((\\( *\\w *: *[a-zA-Z ]+ *\\))*[a-zA-Z]+)*")
                    .matcher(currentStringToken.getString());
            while (regexMatcher.find()) {
                int start = regexMatcher.start() + currentStringToken.getStartCoordinate();
                int end = regexMatcher.end() + currentStringToken.getStartCoordinate();
                FoundGroupEdgePositions foundGroupEdgePositions = findGroupEdgePositions(start, end);
                validateGroupEdgePositions(foundGroupEdgePositions.groupEdgePositions);
                int fixedLeftBorder = startStick(start) ? 0 : -1;
                int fixedRightBorder = endStick(end - 1) ? -2 : -1;
                NucleotideSequence patternSeq = toNSeq(cutGroupsAndSpaces(start, end));

                foundTokens.add(new FoundToken(new FuzzyMatchPattern(getPatternAligner(start, end), patternSeq,
                        fixedLeftBorder, fixedRightBorder, foundGroupEdgePositions.groupEdgePositions),
                        foundGroupEdgePositions.leftEdgeCoordinate, foundGroupEdgePositions.rightEdgeCoordinate));
            }
        }

        return foundTokens;
    }

    ArrayList<FoundToken> parseAnyPatterns(TokenizedString tokenizedString) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        List<Token> stringTokens = tokenizedString.getTokens(0, tokenizedString.getFullLength()).stream()
                .filter(Token::isString).collect(Collectors.toList());
        for (int i = 0; i < stringTokens.size(); i++) {
            Token currentStringToken = stringTokens.get(i);
            String currentString = currentStringToken.getString();
            Matcher regexMatcher = Pattern.compile(": *\\* *\\)").matcher(currentString);
            while (regexMatcher.find()) {
                int start = currentString.indexOf("*", regexMatcher.start()) + currentStringToken.getStartCoordinate();
                int end = start + 1;
                if (((i != 0) && !currentString.substring(0, start).contains("\\"))
                        || ((i != stringTokens.size() - 1) && !currentString.substring(end).contains("\\")))
                    throw new ParserException("'*' pattern is invalid if there are other patterns in the same read, "
                            + "use 'N{*}' instead!");

                ArrayList<GroupEdge> groupEdges = new ArrayList<>();
                ArrayList<FoundGroupName> groupNamesLeft = findGroupNames(start, true);
                ArrayList<FoundGroupName> groupNamesRight = findGroupNames(end, false);
                groupEdges.addAll(groupNamesLeft.stream()
                        .map(gn -> new GroupEdge(gn.name, true)).collect(Collectors.toList()));
                groupEdges.addAll(groupNamesRight.stream()
                        .map(gn -> new GroupEdge(gn.name, false)).collect(Collectors.toList()));
                validateGroupEdges(groupEdges, true, false);
                int tokenStart = groupNamesLeft.stream().mapToInt(fgn -> fgn.edgeCoordinate).min().orElse(start);
                int tokenEnd = groupNamesRight.stream().mapToInt(fgn -> fgn.edgeCoordinate).max().orElse(end - 1) + 1;

                foundTokens.add(new FoundToken(new AnyPattern(getPatternAligner(start, end), groupEdges),
                        tokenStart, tokenEnd));
            }
        }

        return foundTokens;
    }

    /**
     * This parser launches 2 times: once for left border filters and then for right border filters. On first pass it
     * expects nucleotide sequences to be FuzzyMatchPattern only, on second pass they can be FilterPattern
     * from 1st pass.
     *
     * @param tokenizedString tokenized string object for query string
     * @param left true when running for left border filters, false for right border filters
     * @return list of found tokens
     */
    ArrayList<FoundToken> parseBorderFilters(TokenizedString tokenizedString, boolean left) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        if (left) {
            Matcher leftMatcher = Pattern.compile("(<\\{ *\\d+ *}|<+)[a-zA-Z]+").matcher(query);
            while (leftMatcher.find()) {
                int start = leftMatcher.start();
                int end = leftMatcher.end();
                NucleotideSequence seq;
                int minNucleotides;
                if (leftMatcher.group().contains("{")) {
                    BorderFilterBracesPair borderFilterBracesPair = borderFilterBracesPairs.stream()
                            .filter(bp -> bp.leftBorder)
                            .filter(bp -> (bp.bracesPair.start > start) && (bp.bracesPair.end < end))
                            .findFirst().orElseThrow(() -> new IllegalStateException(
                                    "BorderFilterBracesPair not found for " + leftMatcher.group()));

                    seq = toNSeq(query.substring(borderFilterBracesPair.bracesPair.end + 1, end));
                    minNucleotides = seq.size() - borderFilterBracesPair.numberOfRepeats;
                } else {
                    String foundString = leftMatcher.group();
                    seq = toNSeq(foundString.replace("<", ""));
                    minNucleotides = seq.size() - countCharacters(foundString, '<');
                }
                if (minNucleotides < 0)
                    throw new ParserException("Invalid border filter, not enough nucleotides: " + leftMatcher.group());
                boolean useTarget = !isSpecificCharBeforeStopChar(query, start, true, true,
                        "[]", "\\", null);
                BorderFilter filter = new BorderFilter(getPatternAligner(start, end), true, seq, minNucleotides,
                        useTarget);
                Token fuzzyMatchPatternToken = tokenizedString.getTokens(start, end).stream()
                        .filter(t -> !t.isString()).findFirst().orElseThrow(() -> new IllegalStateException(
                                "Parsed FuzzyMatchPattern not found for BorderFilter: " + leftMatcher.group()));
                FuzzyMatchPattern fuzzyMatchPattern = fuzzyMatchPatternToken.getSpecificPattern(FuzzyMatchPattern.class);
                foundTokens.add(new FoundToken(new FilterPattern(getPatternAligner(start, end), filter,
                        fuzzyMatchPattern), start, fuzzyMatchPatternToken.getStartCoordinate()
                        + fuzzyMatchPatternToken.getLength()));
            }
        } else {
            Matcher rightMatcher = Pattern.compile("[a-zA-Z]+(>\\{ *\\d+ *}|>+)").matcher(query);
            while (rightMatcher.find()) {
                int start = rightMatcher.start();
                int end = rightMatcher.end();
                NucleotideSequence seq;
                int minNucleotides;
                if (rightMatcher.group().contains("{")) {
                    BorderFilterBracesPair borderFilterBracesPair = borderFilterBracesPairs.stream()
                            .filter(bp -> !bp.leftBorder)
                            .filter(bp -> (bp.bracesPair.start > start) && (bp.bracesPair.end < end))
                            .findFirst().orElseThrow(() -> new IllegalStateException(
                                    "BorderFilterBracesPair not found for " + rightMatcher.group()));

                    seq = toNSeq(query.substring(start, borderFilterBracesPair.bracesPair.start - 1));
                    minNucleotides = seq.size() - borderFilterBracesPair.numberOfRepeats;
                } else {
                    String foundString = rightMatcher.group();
                    seq = toNSeq(foundString.replace(">", ""));
                    minNucleotides = seq.size() - countCharacters(foundString, '>');
                }
                if (minNucleotides < 0)
                    throw new ParserException("Invalid border filter, not enough nucleotides: " + rightMatcher.group());
                boolean useTarget = !isSpecificCharBeforeStopChar(query, end - 1, false, true,
                        "[]", "\\", null);
                BorderFilter filter = new BorderFilter(getPatternAligner(start, end), false, seq, minNucleotides,
                        useTarget);
                Token patternToken = tokenizedString.getTokens(start, end).stream()
                        .filter(t -> !t.isString()).findFirst().orElseThrow(() -> new IllegalStateException(
                                "Parsed pattern not found for BorderFilter: " + rightMatcher.group()));
                SinglePattern singlePattern = patternToken.getSinglePattern();
                if (!FuzzyMatchPattern.class.isAssignableFrom(singlePattern.getClass())
                        && !FilterPattern.class.isAssignableFrom(singlePattern.getClass()))
                    throw new IllegalStateException("Unexpected class for BorderFilter operand: "
                            + singlePattern.getClass().getName());
                foundTokens.add(new FoundToken(new FilterPattern(getPatternAligner(start, end), filter,
                        singlePattern), patternToken.getStartCoordinate(), end));
            }
        }

        return foundTokens;
    }

    /**
     * This function will remove space strings by merging them into neighbor patterns.
     *
     * @param tokenizedString tokenized string object for query string
     * @return list of enlarged pattern tokens that will overwrite neighbor space strings
     */
    ArrayList<FoundToken> removeSpaceStrings(TokenizedString tokenizedString) {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        ArrayList<Token> tokens = tokenizedString.getTokens(0, tokenizedString.getFullLength());
        boolean firstTokenIsSpaceString = tokens.get(0).isString()
                && tokens.get(0).getString().replace(" ", "").equals("");
        if (firstTokenIsSpaceString && (tokens.size() == 2))
            foundTokens.add(new FoundToken(tokens.get(1).getPattern(), 0, tokenizedString.getFullLength()));
        else
            for (int i = 1; i < tokens.size(); i++) {
                Token currentToken = tokens.get(i);
                if (currentToken.isString()
                        && currentToken.getString().replace(" ", "").equals("")) {
                    if ((i == 2) && firstTokenIsSpaceString)
                        foundTokens.add(new FoundToken(tokens.get(1).getPattern(),
                                0, currentToken.getStartCoordinate() + currentToken.getLength()));
                    else {
                        Token previousToken = tokens.get(i - 1);
                        foundTokens.add(new FoundToken(previousToken.getPattern(), previousToken.getStartCoordinate(),
                                currentToken.getStartCoordinate() + currentToken.getLength()));
                    }
                }
            }

        return foundTokens;
    }

    /**
     * This function will parse sequences of already parsed patterns. It will be called multiple times.
     *
     * @param tokenizedString tokenized string object for query string
     * @return list of found tokens
     */
    ArrayList<FoundToken> parseSequencePatterns(TokenizedString tokenizedString) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        ArrayList<Token> tokens = tokenizedString.getTokens(0, tokenizedString.getFullLength());
        boolean sequenceStarted = false;
        int sequenceStart = 0;
        for (int i = 0; i <= tokens.size(); i++) {
            if ((i == tokens.size()) || tokens.get(i).isString()) {
                if (sequenceStarted) {
                    if (sequenceStart < i - 1) {
                        SinglePattern[] operands = new SinglePattern[i - sequenceStart];
                        for (int j = sequenceStart; j < i; j++)
                            operands[j - sequenceStart] = tokens.get(j).getSinglePattern();
                        int sequenceTokenStart = tokens.get(sequenceStart).getStartCoordinate();
                        int sequenceTokenEnd = (i == tokens.size()) ? tokenizedString.getFullLength()
                                : tokens.get(i).getStartCoordinate();
                        validateGroupEdges(false, false, true, operands);
                        foundTokens.add(new FoundToken(new SequencePattern(getPatternAligner(sequenceTokenStart,
                                sequenceTokenEnd), operands), sequenceTokenStart, sequenceTokenEnd));
                    }
                    sequenceStarted = false;
                }
            } else if (!sequenceStarted) {
                sequenceStart = i;
                sequenceStarted = true;
            }
        }

        return foundTokens;
    }

    /**
     * Parse score filters and remove square brackets around completely parsed patterns by merging them into patterns.
     * It will parse score filters only around completely parsed patterns, and it will be called multiple times.
     *
     * @param tokenizedString tokenized string object for query string
     * @return list of found tokens
     */
    ArrayList<FoundToken> parseScoreFilters(TokenizedString tokenizedString) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        ArrayList<Token> tokens = tokenizedString.getTokens(0, tokenizedString.getFullLength());
        for (int i = 1; i < tokens.size() - 1; i++) {
            Token previousToken = tokens.get(i - 1);
            Token currentToken = tokens.get(i);
            Token nextToken = tokens.get(i + 1);
            if (!currentToken.isString() && previousToken.isString() && nextToken.isString()) {
                String previousString = previousToken.getString();
                String nextString = nextToken.getString();
                boolean bracketsFound = false;
                int bracketsRelativeStart = 0;
                int bracketsRelativeEnd = 0;
                boolean noMoreNestedBrackets = false;
                while (!noMoreNestedBrackets) {
                    Matcher previousMatcher = Pattern.compile(" *\\[ *(-?\\d+ *:)? *$").matcher(previousString);
                    if (previousMatcher.find()) {
                        Matcher nextMatcher = Pattern.compile("^ *] *").matcher(nextString);
                        if (nextMatcher.find()) {
                            bracketsFound = true;
                            bracketsRelativeStart = previousMatcher.start();
                            bracketsRelativeEnd = nextMatcher.end();
                            previousString = previousString.substring(0, bracketsRelativeStart);
                            nextString = nextString.substring(bracketsRelativeEnd);
                        } else
                            noMoreNestedBrackets = true;
                    } else
                        noMoreNestedBrackets = true;
                }
                if (bracketsFound) {
                    int tokenStart = bracketsRelativeStart + previousToken.getStartCoordinate();
                    int tokenEnd = bracketsRelativeEnd + nextToken.getStartCoordinate();
                    FoundToken foundToken;
                    List<ScoreThreshold> foundScoreThresholds = scoreThresholds.stream()
                            .filter(st -> (st.start >= tokenStart) && (st.start < currentToken.getStartCoordinate()))
                            .collect(Collectors.toList());
                    if (!foundScoreThresholds.isEmpty()) {
                        long scoreThreshold = foundScoreThresholds.stream().mapToLong(st -> st.threshold).max()
                                .orElseThrow(IllegalStateException::new);
                        foundToken = new FoundToken(SinglePattern.class.isAssignableFrom(
                                currentToken.getPattern().getClass())
                                ? wrapWithScoreFilter(currentToken.getSinglePattern(), scoreThreshold)
                                : wrapWithScoreFilter(currentToken.getMultipleReadsOperator(), scoreThreshold),
                                tokenStart, tokenEnd);
                    } else
                        foundToken = new FoundToken(currentToken.getPattern(), tokenStart, tokenEnd);
                    foundTokens.add(foundToken);
                }
            }
        }

        return foundTokens;
    }

    /**
     * This function will parse operators with specified sign that are in brackets with specified nested level.
     * It will be called from loop with decreasing nested level, once for each operator on every nested
     * level, starting from operators with higher priority. Nested level -1 means to parse operators outside
     * of brackets. Then this function will be called once more for each nested level to parse MultiPatterns.
     *
     * @param tokenizedString tokenized string object for query string
     * @param operatorRegexp regular expression for operator sign
     * @param nestedLevel current nested level of brackets where to parse; -1 means to parse outside of brackets
     * @return list of found tokens
     */
    ArrayList<FoundToken> parseSingleReadOperators(TokenizedString tokenizedString, String operatorRegexp,
            int nestedLevel) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        List<BracketsPair> bracketsWithThisNestedLevel = (nestedLevel == -1)
                ? Collections.singletonList(new BracketsPair(SQUARE, -1, query.length(), -1))
                : squareBracketsPairs.stream().filter(bp -> bp.nestedLevel == nestedLevel).collect(Collectors.toList());
        for (BracketsPair currentBrackets : bracketsWithThisNestedLevel) {
            ArrayList<Token> tokens = tokenizedString.getTokens(getBracketsContentStart(currentBrackets),
                    currentBrackets.end);
            boolean sequenceStarted = false;
            int sequenceStart = 0;
            for (int i = 0; i <= tokens.size(); i++) {
                if ((i == tokens.size()) || (tokens.get(i).isString()
                        && !tokens.get(i).getString().matches(operatorRegexp))) {
                    if (sequenceStarted) {
                        if (sequenceStart < i - 2) {
                            int numOperands = (i - sequenceStart + 1) / 2;
                            SinglePattern[] operands = new SinglePattern[numOperands];
                            for (int j = 0; j < numOperands; j++)
                                operands[j] = tokens.get(sequenceStart + j * 2).getSinglePattern();
                            int sequenceTokenStart = tokens.get(sequenceStart).getStartCoordinate();
                            int sequenceTokenEnd = tokens.get(i - 1).getStartCoordinate()
                                    + tokens.get(i - 1).getLength();
                            FoundToken foundToken;
                            if (operatorRegexp.contains("+")) {
                                validateGroupEdges(false, false, true, operands);
                                foundToken = new FoundToken(new PlusPattern(getPatternAligner(sequenceTokenStart,
                                        sequenceTokenEnd), operands), sequenceTokenStart, sequenceTokenEnd);
                            } else if (operatorRegexp.contains("&")) {
                                validateGroupEdges(true, false, true, operands);
                                foundToken = new FoundToken(new AndPattern(getPatternAligner(sequenceTokenStart,
                                        sequenceTokenEnd), operands), sequenceTokenStart, sequenceTokenEnd);
                            } else if (operatorRegexp.contains("|")) {
                                validateGroupEdges(true, true, true, operands);
                                foundToken = new FoundToken(new OrPattern(getPatternAligner(sequenceTokenStart,
                                        sequenceTokenEnd), operands), sequenceTokenStart, sequenceTokenEnd);
                            } else if (operatorRegexp.contains("\\\\")) {
                                validateGroupEdges(true, false, true, operands);
                                foundToken = new FoundToken(new MultiPattern(getPatternAligner(sequenceTokenStart,
                                        sequenceTokenEnd), operands), sequenceTokenStart, sequenceTokenEnd);
                            } else
                                throw new IllegalArgumentException("Invalid operator regexp: " + operatorRegexp);
                            foundTokens.add(foundToken);
                        }
                        sequenceStarted = false;
                    }
                } else if (!sequenceStarted && !tokens.get(i).isString()) {
                    sequenceStart = i;
                    sequenceStarted = true;
                }
            }
        }

        return foundTokens;
    }

    /**
     * This function will parse multiple read operators with specified sign that are in brackets with specified
     * nested level. It will be called from loop with decreasing nested level, once for each operator on every nested
     * level, starting from operators with higher priority. Nested level -1 means to parse operators outside
     * of brackets.
     *
     * @param tokenizedString tokenized string object for query string
     * @param operatorRegexp regular expression for operator sign
     * @param nestedLevel current nested level of brackets where to parse; -1 means to parse outside of brackets
     * @return list of found tokens
     */
    ArrayList<FoundToken> parseMultiReadOperators(TokenizedString tokenizedString, String operatorRegexp,
            int nestedLevel) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        List<BracketsPair> bracketsWithThisNestedLevel = (nestedLevel == -1)
                ? Collections.singletonList(new BracketsPair(SQUARE, 0, query.length(), -1))
                : squareBracketsPairs.stream().filter(bp -> bp.nestedLevel == nestedLevel).collect(Collectors.toList());
        for (BracketsPair currentBrackets : bracketsWithThisNestedLevel) {
            ArrayList<Token> tokens = tokenizedString.getTokens(getBracketsContentStart(currentBrackets),
                    currentBrackets.end);
            boolean sequenceStarted = false;
            int sequenceStart = 0;
            for (int i = 0; i <= tokens.size(); i++) {
                /* for NotOperator, every single pattern will be treated as sequence, and then it will be checked
                   that previous token matches operatorRegexp */
                if ((i == tokens.size()) || (tokens.get(i).isString()
                        && !tokens.get(i).getString().matches(operatorRegexp))
                        || (sequenceStarted && operatorRegexp.contains("~"))) {
                    if (sequenceStarted) {
                        if (operatorRegexp.contains("~")) {
                            if ((i > 1) && tokens.get(i - 2).isString() &&
                                    tokens.get(i - 2).getString().matches(operatorRegexp)) {
                                int tokenStart = tokens.get(i - 2).getStartCoordinate();
                                int tokenEnd = tokens.get(i - 1).getStartCoordinate() + tokens.get(i - 1).getLength();
                                MultipleReadsOperator operand = tokens.get(i - 1).getMultipleReadsOperator();
                                validateGroupEdges(false, true, false, operand);
                                foundTokens.add(new FoundToken(new NotOperator(getPatternAligner(tokenStart, tokenEnd),
                                        operand), tokenStart, tokenEnd));
                            }
                        } else if (sequenceStart < i - 2) {
                            int numOperands = (i - sequenceStart + 1) / 2;
                            MultipleReadsOperator[] operands = new MultipleReadsOperator[numOperands];
                            for (int j = 0; j < numOperands; j++)
                                operands[j] = tokens.get(sequenceStart + j * 2).getMultipleReadsOperator();
                            int sequenceTokenStart = tokens.get(sequenceStart).getStartCoordinate();
                            int sequenceTokenEnd = tokens.get(i - 1).getStartCoordinate()
                                    + tokens.get(i - 1).getLength();
                            MultipleReadsOperator multipleReadsOperator;
                            if (operatorRegexp.contains("&")) {
                                validateGroupEdges(true, false, true, operands);
                                multipleReadsOperator = new AndOperator(getPatternAligner(sequenceTokenStart,
                                        sequenceTokenEnd), operands);
                            } else if (operatorRegexp.contains("|")) {
                                validateGroupEdges(true, true, true, operands);
                                multipleReadsOperator = new OrOperator(getPatternAligner(sequenceTokenStart,
                                        sequenceTokenEnd), operands);
                            } else
                                throw new IllegalArgumentException("Invalid operator regexp: " + operatorRegexp);
                            foundTokens.add(new FoundToken(multipleReadsOperator, sequenceTokenStart, sequenceTokenEnd));
                        }
                        sequenceStarted = false;
                    }
                } else if (!sequenceStarted && !tokens.get(i).isString()) {
                    sequenceStart = i;
                    sequenceStarted = true;
                }
            }
        }

        return foundTokens;
    }

    /**
     * Return return list of names of groups that have their left edges on left of this pattern (if onLeft == true)
     * or has their right edges on the right of this pattern (if onLeft == false), without any patterns between this
     * pattern and group edges.
     *
     * @param position position where to start the search; exclusive
     * @param onLeft true to search group names on the left from this pattern, false to search group closing parentheses
     *               on the right from this pattern
     * @return list of names of groups for found group edges, and their coordinates
     */
    private ArrayList<FoundGroupName> findGroupNames(int position, boolean onLeft) {
        ArrayList<FoundGroupName> foundGroupNames = new ArrayList<>();
        int currentPosition = position;
        int closestGroupIndex = getClosestGroupByPosition(onLeft ? position : position + 1, onLeft);
        while (closestGroupIndex != -1) {
            NormalSyntaxGroupName currentGroupName = groupNames.get(closestGroupIndex);
            String intermediateSubstring = onLeft ? query.substring(currentGroupName.end + 1,
                    currentPosition) : query.substring(currentPosition + 1, currentGroupName.bracketsPair.end);
            if (intermediateSubstring.matches(".*[a-zA-Z()*].*"))
                break;
            currentPosition = onLeft ? currentGroupName.start : currentGroupName.bracketsPair.end;
            foundGroupNames.add(new FoundGroupName(currentGroupName.name, currentPosition));
            closestGroupIndex = getClosestGroupByPosition(onLeft ? currentPosition : currentPosition + 1, onLeft);
        }
        return foundGroupNames;
    }

    /**
     * Find closest index in group names list by position in query string; return -1 if index not found.
     *
     * @param position position in query string, inclusive (if index is on this position, it will be returned)
     * @param toLeft true: search group name to the left from position,
     *               false: search group closing parenthesis to the right
     * @return found index in list of group names, or -1 if not found
     */
    private int getClosestGroupByPosition(int position, boolean toLeft) {
        for (int i = 0; i < groupNames.size(); i++) {
            if (toLeft && (position >= groupNames.get(i).end)
                    && ((i == groupNames.size() - 1) || (position < groupNames.get(i + 1).start)))
                return i;
            if (!toLeft && (position <= groupNames.get(i).bracketsPair.end)
                    && ((i == 0) || (position > groupNames.get(i - 1).end))
                    && ((i != 0) || (groupNames.size() == 1)))
                return i;
        }
        return -1;
    }

    /**
     * Get group edge positions for FuzzyMatchPattern from specified range in query string, including groups on left
     * and right edges; and coordinates of leftmost and rightmost edges if there are groups on the left or right.
     *
     * @param start start of FuzzyMatchPattern, inclusive
     * @param end end of FuzzyMatchPattern, exclusive
     * @return found group edge positions and edge coordinates
     */
    private FoundGroupEdgePositions findGroupEdgePositions(int start, int end) {
        ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
        int patternStringLength = end - start;
        int ignoredCharactersCount = 0;
        int ignoredCharactersSearchPosition = start;
        for (NormalSyntaxGroupName groupName : groupNames) {
            int groupStart = groupName.start - start;
            int groupNameEnd = groupName.end - start;
            int groupEnd = groupName.bracketsPair.end - start;
            if ((groupStart > 0) && (groupStart < patternStringLength - 1)) {
                if (groupNameEnd < patternStringLength - 1) {
                    ignoredCharactersCount += countCharacters(query.substring(ignoredCharactersSearchPosition,
                            groupName.start), ' ');
                    ignoredCharactersSearchPosition = groupName.end;
                    groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName.name, true),
                            groupStart - ignoredCharactersCount));
                    ignoredCharactersCount += groupNameEnd - groupStart + 1;
                } else
                    throw new IllegalStateException("FuzzyMatchPattern: start=" + start + ", end=" + end
                            + ", group name: start=" + groupName.start + ", end=" + groupName.end);
            }
            if ((groupEnd > 0) && (groupEnd < patternStringLength - 1)) {
                ignoredCharactersCount += countCharacters(query.substring(ignoredCharactersSearchPosition,
                        groupName.bracketsPair.end), ' ');
                ignoredCharactersSearchPosition = groupName.bracketsPair.end;
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName.name, false),
                        groupEnd - ignoredCharactersCount));
                ignoredCharactersCount++;
            }
        }
        ignoredCharactersCount += countCharacters(query.substring(ignoredCharactersSearchPosition, end), ' ');

        ArrayList<FoundGroupName> groupNamesLeft = findGroupNames(start, true);
        ArrayList<FoundGroupName> groupNamesRight = findGroupNames(end - 1, false);
        groupEdgePositions.addAll(groupNamesLeft.stream()
                .map(gn -> new GroupEdgePosition(new GroupEdge(gn.name, true), 0))
                .collect(Collectors.toList()));
        final int patternLength = end - start - ignoredCharactersCount;
        groupEdgePositions.addAll(groupNamesRight.stream()
                .map(gn -> new GroupEdgePosition(new GroupEdge(gn.name, false), patternLength))
                .collect(Collectors.toList()));
        int leftEdgeCoordinate = groupNamesLeft.stream().mapToInt(fgn -> fgn.edgeCoordinate).min().orElse(start);
        int rightEdgeCoordinate = groupNamesRight.stream().mapToInt(fgn -> fgn.edgeCoordinate).max()
                .orElse(end - 1) + 1;

        return new FoundGroupEdgePositions(groupEdgePositions, leftEdgeCoordinate, rightEdgeCoordinate);
    }

    /**
     * Check is this pattern should stick to start (check "^" before the pattern).
     *
     * @param position pattern start position, inclusive
     * @return true if this pattern should stick to target start
     */
    private boolean startStick(int position) {
        int markerPosition = -1;
        for (int i = 0; i < startStickMarkers.size(); i++) {
            if ((position > startStickMarkers.get(i)) && ((i == startStickMarkers.size() - 1)
                    || (position < startStickMarkers.get(i + 1))))
                markerPosition = startStickMarkers.get(i);
        }

        return (markerPosition != -1) && !query.substring(markerPosition + 1, position).matches(".*\\\\.*")
                && !isAnyNucleotide(markerPosition + 1, position);
    }

    /**
     * Check is this pattern should stick to end (check "$" after the pattern).
     *
     * @param position pattern end position, inclusive
     * @return true if this pattern should stick to target end
     */
    private boolean endStick(int position) {
        int markerPosition = -1;
        for (int i = 0; i < endStickMarkers.size(); i++) {
            if ((position < endStickMarkers.get(i)) && ((i == 0) || (position > endStickMarkers.get(i - 1)))
                    && ((i != 0) || (endStickMarkers.size() == 1)))
                markerPosition = endStickMarkers.get(i);
        }

        return (markerPosition != -1) && !query.substring(position + 1, markerPosition).matches(".*\\\\.*")
                && !isAnyNucleotide(position + 1, markerPosition);
    }

    /**
     * Returns true if there is any nucleotide in query substring, otherwise false.
     *
     * @param start substring start position, inclusive
     * @param end substring end position, exclusive
     * @return true if there is any nucleotide in query substring, otherwise false
     */
    private boolean isAnyNucleotide(int start, int end) {
        for (int currentPosition = start; currentPosition < end; currentPosition++) {
            final int position = currentPosition;
            if (query.substring(position, position + 1).matches("[a-zA-Z]")
                    && groupNames.stream().noneMatch(gn -> (gn.start < position) && (gn.end > position)))
                return true;
        }
        return false;
    }

    /**
     * Cuts group start and end tokens and spaces from FuzzyMatchPattern substring.
     *
     * @param start FuzzyMatchPattern start, inclusive
     * @param end FuzzyMatchPattern end, exclusive
     * @return cutted string that contains only nucleotide sequence for FuzzyMatchPattern
     */
    private String cutGroupsAndSpaces(int start, int end) {
        StringBuilder result = new StringBuilder();
        for (int currentPosition = start; currentPosition < end; currentPosition++) {
            final int position = currentPosition;
            if ((query.charAt(position) != ' ')
                    && groupNames.stream().noneMatch(gn -> (gn.start <= position) && (gn.end >= position)))
                result.append(query.charAt(position));
        }
        return result.toString();
    }

    private SinglePattern wrapWithScoreFilter(SinglePattern singlePattern, long scoreThreshold) {
        return new FilterPattern(patternAligner, new ScoreFilter(scoreThreshold), singlePattern);
    }

    private MultipleReadsOperator wrapWithScoreFilter(MultipleReadsOperator multiReadPattern, long scoreThreshold) {
        return new MultipleReadsFilterPattern(patternAligner, new ScoreFilter(scoreThreshold), multiReadPattern);
    }

    /**
     * Returns start coordinate of contents of square brackets pair. If there is score filter, it will return
     * the position after colon.
     *
     * @param bracketsPair square brackets pair; can start with -1 if this is virtual brackets pair that includes
     *                     the entire query
     * @return start coordinate of contents of square brackets pair
     */
    private int getBracketsContentStart(BracketsPair bracketsPair) {
        if (bracketsPair.bracketsType != SQUARE)
            throw new IllegalArgumentException("getBracketContentStart called with brackets of type "
                    + bracketsPair.bracketsType);
        boolean isScoreThreshold = scoreThresholds.stream().anyMatch(st -> st.start == bracketsPair.start);
        if (isScoreThreshold) {
            int colonPosition = query.substring(bracketsPair.start + 1, bracketsPair.end).indexOf(":");
            if (colonPosition == -1)
                throw new IllegalStateException("No colon inside brackets with score threshold: "
                        + query.substring(bracketsPair.start + 1, bracketsPair.end));
            else
                return bracketsPair.start + colonPosition + 2;
        } else
            return bracketsPair.start + 1;
    }

    /**
     * Find score threshold for specified range and return PatternAligner with this threshold. If there is no score
     * threshold, return pattern aligner without changing its threshold.
     *
     * @param start start of range, inclusive
     * @param end end of range, exclusive
     * @return PatternAligner with updated score threshold for the specified range
     */
    private PatternAligner getPatternAligner(int start, int end) {
        int currentNestedLevel = -1;
        long currentThreshold = 0;
        for (ScoreThreshold scoreThreshold : scoreThresholds)
            if (scoreThreshold.contains(start, end) && (scoreThreshold.nestedLevel > currentNestedLevel)) {
                currentNestedLevel = scoreThreshold.nestedLevel;
                currentThreshold = scoreThreshold.threshold;
            }
        if (currentNestedLevel == -1)
            return patternAligner;
        else
            return patternAligner.overridePenaltyThreshold(currentThreshold);
    }

    private static class FoundGroupName {
        final String name;
        final int edgeCoordinate;

        /**
         * Return list item for findGroupNames(): group name and edge coordinate - open parenthesis for left group edge
         * or closed parenthesis for right group edge; both inclusive.
         *
         * @param name found group name
         * @param edgeCoordinate group edge coordinate, inclusive
         */
        FoundGroupName(String name, int edgeCoordinate) {
            this.name = name;
            this.edgeCoordinate = edgeCoordinate;
        }
    }

    private static class FoundGroupEdgePositions {
        final ArrayList<GroupEdgePosition> groupEdgePositions;
        final int leftEdgeCoordinate;
        final int rightEdgeCoordinate;

        /**
         * Return values for findGroupEdgePositions(): list of group edge positions; leftmost group left edge coordinate
         * if there are group edges on the left from current pattern or pattern left edge if there are no group edges
         * on the left; and similarly right edge coordinate.
         *
         * @param groupEdgePositions list of group edge positions
         * @param leftEdgeCoordinate left edge coordinate, inclusive
         * @param rightEdgeCoordinate right edge coordinate, exclusive
         */
        FoundGroupEdgePositions(ArrayList<GroupEdgePosition> groupEdgePositions, int leftEdgeCoordinate,
                                int rightEdgeCoordinate) {
            this.groupEdgePositions = groupEdgePositions;
            this.leftEdgeCoordinate = leftEdgeCoordinate;
            this.rightEdgeCoordinate = rightEdgeCoordinate;
        }
    }
}
