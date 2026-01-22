import { useState, useEffect, useRef } from 'react';
import { resizeImage, validateImage } from '../lib/imageUtils';

interface ChatInterfaceProps {
  conversationId: string;
  requestType: 'RETURN' | 'COMPLAINT';
  initialImages?: File[];
  onNewRequest: () => void;
}

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

/**
 * Formats message content for display, removing JSON code blocks and rendering markdown
 */
function formatMessageContent(content: string): React.ReactNode {
  // Remove JSON code blocks (```json ... ```) but keep the text content
  // This removes the structured JSON response and shows only the explanation
  let filteredContent = content;
  
  // Remove JSON code blocks
  filteredContent = filteredContent.replace(/```json\s*[\s\S]*?```/g, '');
  // Also remove generic code blocks that might contain JSON
  filteredContent = filteredContent.replace(/```\s*\{[\s\S]*?\}\s*```/g, '');
  
  // Remove section headers that are part of the JSON response structure
  filteredContent = filteredContent.replace(/\*\*JSON Response:\*\*/gi, '');
  filteredContent = filteredContent.replace(/\*\*Explanation:\*\*/gi, '');
  filteredContent = filteredContent.replace(/JSON Response:/gi, '');
  filteredContent = filteredContent.replace(/Explanation:/gi, '');
  
  // Remove common introductory phrases that reference JSON
  filteredContent = filteredContent.replace(/Here's the analysis of the image and the requested response:/gi, '');
  filteredContent = filteredContent.replace(/Here's the analysis:/gi, '');
  filteredContent = filteredContent.replace(/Here's the requested response:/gi, '');
  
  // Clean up extra whitespace and newlines
  filteredContent = filteredContent.replace(/\n{3,}/g, '\n\n');
  filteredContent = filteredContent.replace(/^\s+|\s+$/g, ''); // Trim start and end
  filteredContent = filteredContent.trim();
  
  // Parse markdown to React elements
  return parseMarkdown(filteredContent);
}

/**
 * Simple markdown parser for basic formatting
 */
function parseMarkdown(text: string): React.ReactNode {
  const parts: React.ReactNode[] = [];
  let key = 0;

  // First, normalize the text and split list items that appear inline
  let processedText = text;
  
  // Simple and aggressive: find any "*   " (asterisk + 2+ spaces) that appears after any text
  // and split it into a new line. This handles all cases where list items appear inline.
  // Pattern: any character (not newline) followed by * and 2+ spaces
  processedText = processedText.replace(/([^\n])\*\s{2,}/g, '$1\n*   ');
  
  // Also handle cases where * appears with single space after punctuation (less common but possible)
  processedText = processedText.replace(/([:\\.])\s*\*\s([A-Z])/g, '$1\n* $2');
  
  // Process line by line to handle lists
  const lines = processedText.split('\n');
  let currentListItems: React.ReactNode[] = [];
  let inList = false;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    // Check if this is a list item (starts with * or - followed by spaces)
    // Pattern: optional leading whitespace, then * or -, then one or more spaces, then content
    const listItemMatch = line.match(/^[\s]*[\*\-]\s{1,}(.+)$/);
    if (listItemMatch) {
      if (!inList) {
        // Start new list
        inList = true;
        currentListItems = [];
      }
      
      // Parse the list item content (may contain bold/italic)
      const itemContent = parseInlineMarkdown(listItemMatch[1].trim(), key++);
      currentListItems.push(<li key={`item-${key++}`}>{itemContent}</li>);
    } else {
      // Not a list item
      if (inList) {
        // Close the current list
        parts.push(
          <ul key={`list-${key++}`} className="list-disc list-inside my-2 space-y-1">
            {currentListItems}
          </ul>
        );
        currentListItems = [];
        inList = false;
      }
      
      // Regular paragraph line
      if (line.trim()) {
        const parsedLine = parseInlineMarkdown(line, key++);
        parts.push(
          <p key={`para-${key++}`} className="mb-2">
            {parsedLine}
          </p>
        );
      } else if (i < lines.length - 1) {
        // Empty line - add spacing (but not at the end)
        parts.push(<br key={`br-${key++}`} />);
      }
    }
  }
  
  // Close any remaining list
  if (inList && currentListItems.length > 0) {
    parts.push(
      <ul key={`list-${key++}`} className="list-disc list-inside my-2 space-y-1">
        {currentListItems}
      </ul>
    );
  }

  return <div className="whitespace-pre-wrap">{parts}</div>;
}

/**
 * Parse inline markdown (bold, italic) within a line
 */
function parseInlineMarkdown(text: string, baseKey: number): React.ReactNode {
  const parts: React.ReactNode[] = [];
  let remaining = text;
  let key = 0;
  let lastIndex = 0;

  // Process bold (**text**)
  const boldRegex = /\*\*([^*]+)\*\*/g;
  let match;
  const boldMatches: Array<{ start: number; end: number; text: string }> = [];
  
  while ((match = boldRegex.exec(text)) !== null) {
    boldMatches.push({
      start: match.index,
      end: match.index + match[0].length,
      text: match[1],
    });
  }

  // Process italic (*text* or _text_) - but not if it's part of **text**
  const italicRegex = /(?<!\*)\*([^*]+)\*(?!\*)/g;
  const italicMatches: Array<{ start: number; end: number; text: string }> = [];
  
  while ((match = italicRegex.exec(text)) !== null) {
    // Check if this italic is not part of a bold
    const isPartOfBold = boldMatches.some(
      (b) => match!.index >= b.start && match!.index < b.end
    );
    if (!isPartOfBold) {
      italicMatches.push({
        start: match.index,
        end: match.index + match[0].length,
        text: match[1],
      });
    }
  }

  // Combine and sort all matches
  const allMatches = [
    ...boldMatches.map((m) => ({ ...m, type: 'bold' as const })),
    ...italicMatches.map((m) => ({ ...m, type: 'italic' as const })),
  ].sort((a, b) => a.start - b.start);

  // Build React elements
  for (const match of allMatches) {
    // Add text before this match
    if (match.start > lastIndex) {
      parts.push(text.substring(lastIndex, match.start));
    }

    // Add the formatted text
    if (match.type === 'bold') {
      parts.push(<strong key={`bold-${baseKey}-${key++}`}>{match.text}</strong>);
    } else {
      parts.push(<em key={`italic-${baseKey}-${key++}`}>{match.text}</em>);
    }

    lastIndex = match.end;
  }

  // Add remaining text
  if (lastIndex < text.length) {
    parts.push(text.substring(lastIndex));
  }

  return parts.length > 0 ? <>{parts}</> : text;
}

export function ChatInterface({
  conversationId,
  requestType,
  initialImages = [],
  onNewRequest,
}: ChatInterfaceProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [uploadingImages, setUploadingImages] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const hasInitialized = useRef(false);

  useEffect(() => {
    // Auto-trigger initial analysis when component mounts with initial images
    if (!hasInitialized.current && initialImages.length > 0) {
      hasInitialized.current = true;
      handleInitialAnalysis(initialImages);
    }
  }, [initialImages]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleInitialAnalysis = async (images: File[]) => {
    setIsLoading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('conversationId', conversationId);
      formData.append('requestType', requestType);
      images.forEach((file) => {
        formData.append('images', file);
      });

      console.log('Sending request to /api/chat...');
      const response = await fetch('/api/chat', {
        method: 'POST',
        body: formData,
      });

      console.log('Response status:', response.status, response.statusText);
      console.log('Response headers:', Object.fromEntries(response.headers.entries()));
      console.log('Response ok:', response.ok);
      console.log('Response body:', response.body);

      if (!response.ok) {
        const errorText = await response.text().catch(() => 'Unknown error');
        console.error('Response not OK. Status:', response.status, 'Body:', errorText);
        throw new Error(`Failed to start analysis: ${response.status} ${response.statusText}`);
      }

      if (!response.body) {
        console.error('Response body is null');
        throw new Error('No response body received');
      }

      await streamResponse(response);
    } catch (err) {
      console.error('Analysis error:', err);
      setError(err instanceof Error ? err.message : 'Failed to start analysis. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const streamResponse = async (response: Response) => {
    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('No response body');
    }

    // Initialize assistant message before streaming starts with "Generating response..."
    setMessages((prev) => [...prev, { role: 'assistant', content: 'Generating response...' }]);

    const decoder = new TextDecoder();
    let buffer = '';
    let rawMessage = ''; // Accumulate all text including JSON blocks
    let isInJsonBlock = false; // Track if we're currently in a JSON block

    // Helper function to filter JSON blocks and return display text
    const filterJsonBlocks = (text: string): { displayText: string; isInJson: boolean } => {
      let displayText = text;
      let inJson = false;
      
      // First, check for raw JSON at the start (without code blocks)
      // Pattern: starts with { and contains "status" (multiline)
      const rawJsonPattern = /^\s*\{[\s\S]*?"status"/;
      if (rawJsonPattern.test(text)) {
        // Try to find the end of the JSON object
        let braceCount = 0;
        let inString = false;
        let escapeNext = false;
        let jsonEndIndex = -1;
        
        for (let i = 0; i < text.length; i++) {
          const char = text[i];
          
          if (escapeNext) {
            escapeNext = false;
            continue;
          }
          
          if (char === '\\') {
            escapeNext = true;
            continue;
          }
          
          if (char === '"' && !escapeNext) {
            inString = !inString;
            continue;
          }
          
          if (!inString) {
            if (char === '{') {
              braceCount++;
            } else if (char === '}') {
              braceCount--;
              if (braceCount === 0) {
                jsonEndIndex = i;
                break;
              }
            }
          }
        }
        
        if (jsonEndIndex >= 0) {
          // Complete JSON found - remove it and keep only text after
          displayText = text.substring(jsonEndIndex + 1).trim();
        } else {
          // Incomplete JSON - we're still in JSON
          inJson = true;
          displayText = '';
        }
      }
      
      // Also handle JSON in code blocks (```json ... ```)
      if (!inJson) {
        // Pattern to detect JSON block start: ```json or ```{
        const jsonBlockStartPattern = /```(json|\{)/;
        
        // Find all complete JSON blocks and remove them
        const jsonBlockRegex = /```(json|\{)[\s\S]*?```/g;
        const completeBlocks = Array.from(displayText.matchAll(jsonBlockRegex));
        
        if (completeBlocks.length > 0) {
          // Remove all complete JSON blocks
          displayText = displayText.replace(jsonBlockRegex, '');
          
          // Check if there's an incomplete JSON block after the last complete one
          const lastCompleteBlock = completeBlocks[completeBlocks.length - 1];
          const afterLastBlock = displayText.substring(lastCompleteBlock.index! + lastCompleteBlock[0].length);
          
          if (jsonBlockStartPattern.test(afterLastBlock)) {
            // Found a JSON block start without closing - we're in JSON
            inJson = true;
            // Remove everything from the JSON start onwards
            const jsonStartIndex = afterLastBlock.search(jsonBlockStartPattern);
            displayText = displayText.substring(0, lastCompleteBlock.index! + lastCompleteBlock[0].length + jsonStartIndex).trim();
          }
        } else {
          // No complete blocks - check for incomplete JSON block start
          if (jsonBlockStartPattern.test(displayText)) {
            const jsonStartMatch = displayText.match(jsonBlockStartPattern);
            if (jsonStartMatch) {
              const jsonStartIndex = displayText.indexOf(jsonStartMatch[0]);
              const afterJsonStart = displayText.substring(jsonStartIndex + jsonStartMatch[0].length);
              
              // Count closing ``` after the start
              const closingBackticks = (afterJsonStart.match(/```/g) || []).length;
              
              if (closingBackticks === 0) {
                // No closing ``` found - we're in an incomplete JSON block
                inJson = true;
                // Show only text before JSON block starts
                displayText = displayText.substring(0, jsonStartIndex).trim();
              } else {
                // Has closing ``` - might be a complete block, check more carefully
                const firstClosingIndex = afterJsonStart.indexOf('```');
                if (firstClosingIndex > 0) {
                  // There's content before closing - might be complete
                  // But if there's more content after, it's incomplete
                  const afterClosing = afterJsonStart.substring(firstClosingIndex + 3);
                  if (jsonBlockStartPattern.test(afterClosing)) {
                    inJson = true;
                    displayText = displayText.substring(0, jsonStartIndex).trim();
                  }
                }
              }
            }
          }
        }
      }
      
      // Remove section headers and introductory phrases
      displayText = displayText.replace(/\*\*JSON Response:\*\*/gi, '');
      displayText = displayText.replace(/\*\*Explanation:\*\*/gi, '');
      displayText = displayText.replace(/JSON Response:/gi, '');
      displayText = displayText.replace(/Explanation:/gi, '');
      displayText = displayText.replace(/Here's the analysis of the image and the requested response:/gi, '');
      displayText = displayText.replace(/Here's the analysis:/gi, '');
      displayText = displayText.replace(/Here's the requested response:/gi, '');
      
      // Clean up whitespace
      displayText = displayText.replace(/\n{3,}/g, '\n\n').trim();
      
      return { displayText, isInJson: inJson };
    };

    try {
      let chunkCount = 0;
      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          console.log('Stream reading completed. Total chunks:', chunkCount, 'Final message length:', rawMessage.length);
          break;
        }

        if (value) {
          chunkCount++;
          const decoded = decoder.decode(value, { stream: true });
          console.log(`Chunk ${chunkCount} received, length:`, decoded.length, 'Preview:', decoded.substring(0, Math.min(100, decoded.length)));
          buffer += decoded;
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          console.log('Processing', lines.length, 'lines from buffer');
          for (const line of lines) {
            const trimmedLine = line.trim();
            
            // Skip empty lines or lines that are just "data:"
            if (!trimmedLine || trimmedLine === 'data:') {
              continue;
            }
            
            // Handle SSE format: data: 0:"text" or data:data: 0:"text" (double prefix from Spring) or just 0:"text"
            let text: string | null = null;
            
            if (trimmedLine.startsWith('data:data: 0:"') || trimmedLine.startsWith('data: 0:"') || trimmedLine.startsWith('data:0:"') || trimmedLine.startsWith('0:"')) {
              // Extract text from Vercel protocol format
              let jsonString: string;
              
              if (trimmedLine.startsWith('data:data: 0:"')) {
                jsonString = trimmedLine.slice(13);
              } else if (trimmedLine.startsWith('data: 0:"')) {
                jsonString = trimmedLine.slice(8);
              } else if (trimmedLine.startsWith('data:0:"')) {
                jsonString = trimmedLine.slice(7);
              } else {
                jsonString = trimmedLine.slice(2);
              }
              
              // Try to parse as JSON string
              try {
                text = JSON.parse(jsonString);
              } catch (e) {
                // If JSON.parse fails, try manual extraction as fallback
                if (jsonString.startsWith('"')) {
                  let endIndex = jsonString.length;
                  for (let i = jsonString.length - 1; i > 0; i--) {
                    if (jsonString[i] === '"') {
                      let backslashes = 0;
                      for (let j = i - 1; j >= 0 && jsonString[j] === '\\'; j--) {
                        backslashes++;
                      }
                      if (backslashes % 2 === 0) {
                        endIndex = i;
                        break;
                      }
                    }
                  }
                  
                  if (endIndex < jsonString.length) {
                    const quotedContent = jsonString.slice(0, endIndex + 1);
                    try {
                      text = JSON.parse(quotedContent);
                    } catch (e2) {
                      // Parsing failed, skip this chunk
                    }
                  }
                }
              }
              
              // Add to raw message
              if (text !== null && text.length > 0) {
                rawMessage += text;
                
                // Filter JSON blocks and get display text
                const { displayText, isInJson: currentlyInJson } = filterJsonBlocks(rawMessage);
                
                // Update state
                const wasInJson = isInJsonBlock;
                isInJsonBlock = currentlyInJson;
                
                // Determine what to display
                let contentToShow: string;
                if (isInJsonBlock) {
                  // We're in a JSON block - show ONLY "Generating response..."
                  contentToShow = 'Generating response...';
                } else {
                  // Not in JSON block - show filtered content (text after JSON blocks)
                  contentToShow = displayText;
                }

                // Update messages with filtered content
                setMessages((prev) => {
                  const lastMessage = prev[prev.length - 1];
                  if (lastMessage && lastMessage.role === 'assistant') {
                    return [
                      ...prev.slice(0, -1),
                      { role: 'assistant', content: contentToShow },
                    ];
                  }
                  return [...prev, { role: 'assistant', content: contentToShow }];
                });
              }
            }
          }
        }
      }
    } catch (error) {
      console.error('Error reading stream:', error);
      console.error('Error details:', {
        name: error instanceof Error ? error.name : 'Unknown',
        message: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined,
      });
      // Don't throw here - let the calling function handle it
      setError('Error reading response stream');
      throw error;
    } finally {
      // Final cleanup: remove any "generating response" and show final filtered content
      const { displayText } = filterJsonBlocks(rawMessage);
      setMessages((prev) => {
        const lastMessage = prev[prev.length - 1];
        if (lastMessage && lastMessage.role === 'assistant') {
          return [
            ...prev.slice(0, -1),
            { role: 'assistant', content: displayText },
          ];
        }
        return [...prev, { role: 'assistant', content: displayText }];
      });
    }
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    setUploadingImages(true);
    setError(null);

    try {
      // Validate and resize images
      const processedFiles: File[] = [];
      for (const file of files) {
        const validation = validateImage(file);
        if (!validation.valid) {
          setError(validation.error || 'Invalid image file');
          setUploadingImages(false);
          return;
        }
        const resized = await resizeImage(file);
        processedFiles.push(resized);
      }

      // Add user message
      setMessages((prev) => [
        ...prev,
        {
          role: 'user',
          content: `Uploaded ${processedFiles.length} image(s) for analysis`,
        },
      ]);

      // Create FormData
      const formData = new FormData();
      formData.append('conversationId', conversationId);
      formData.append('requestType', requestType);
      processedFiles.forEach((file) => {
        formData.append('images', file);
      });

      // Send to backend
      const response = await fetch('/api/chat', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Failed to upload images');
      }

      setIsLoading(true);
      await streamResponse(response);
    } catch (err) {
      setError('Failed to process images. Please try again.');
      console.error('Image upload error:', err);
    } finally {
      setUploadingImages(false);
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.length === 0 && (
          <div className="text-center text-gray-500">
            <p>AI is analyzing your {requestType === 'RETURN' ? 'receipt' : 'defect photos'}...</p>
            <p className="text-sm mt-2">You can upload additional images if needed.</p>
          </div>
        )}
        {messages.map((message, index) => (
          <div
            key={index}
            className={`p-3 rounded ${
              message.role === 'user'
                ? 'bg-blue-100 ml-auto max-w-xs'
                : 'bg-gray-100 mr-auto max-w-2xl'
            }`}
          >
            <div>{formatMessageContent(message.content)}</div>
          </div>
        ))}
        {isLoading && (
          <div className="bg-gray-100 p-3 rounded mr-auto max-w-2xl">
            <p>Analyzing...</p>
          </div>
        )}
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mx-4 mb-2">
          {error}
        </div>
      )}

      {/* Input Area */}
      <div className="border-t p-4 space-y-2">
        <div className="flex gap-2">
          <label className="flex items-center px-4 py-2 bg-gray-200 rounded cursor-pointer hover:bg-gray-300">
            <input
              type="file"
              accept="image/jpeg,image/jpg,image/png,image/webp"
              multiple
              onChange={handleImageUpload}
              disabled={uploadingImages || isLoading}
              className="hidden"
            />
            <span>{uploadingImages ? 'Processing...' : 'Upload Images'}</span>
          </label>
        </div>
        <button
          onClick={onNewRequest}
          className="w-full bg-gray-200 text-gray-700 py-2 px-4 rounded hover:bg-gray-300"
        >
          Start New Request
        </button>
      </div>
    </div>
  );
}
