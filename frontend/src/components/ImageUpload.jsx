import { useState } from 'react';
import './ImageUpload.css';

const ImageUpload = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [preview, setPreview] = useState(null);
    const [productProblemDetails, setProductProblemDetails] = useState('');
    const [analysis, setAnalysis] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        if (file) {
            setSelectedFile(file);
            setPreview(URL.createObjectURL(file));
            setAnalysis(null);
            setError('');
        }
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            setError('Please select an image first.');
            return;
        }

        if (!productProblemDetails) {
            setError('Please provide problem details for the analysis.');
            return;
        }

        setLoading(true);
        setError('');
        setAnalysis(null);

        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('productProblemDetails', productProblemDetails);

        try {
            const response = await fetch('http://localhost:8080/api/images/upload', {
                method: 'POST',
                body: formData,
            });

            if (!response.ok) {
                throw new Error(`Upload failed: ${response.statusText}`);
            }

            const result = await response.text();
            try {
                // Attempt to parse JSON if possible
                const jsonResult = JSON.parse(result);
                setAnalysis(jsonResult);
            } catch (e) {
                // Fallback to raw text if not JSON
                setAnalysis({ raw: result });
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="image-upload-container">
            <h1 className="main-header">Sinsay Claim Analysis</h1>

            <div className="main-grid">
                <div className="left-panel">
                    <div className="upload-section">
                        <div className="input-group">
                            <label>Product Problem Details (what to focus on?)</label>
                            <textarea
                                value={productProblemDetails}
                                onChange={(e) => setProductProblemDetails(e.target.value)}
                                placeholder="e.g. Rozdarty szew pod pachą, widać wystające nitki..."
                            />
                        </div>

                        <div className="file-selection-row">
                            <label htmlFor="file-upload" className="custom-file-upload">
                                Choose Product Image
                            </label>
                            <input
                                id="file-upload"
                                type="file"
                                accept="image/*,.heic,.heif"
                                onChange={handleFileSelect}
                                style={{ display: 'none' }}
                            />
                            <span className="file-name-label">
                                {selectedFile ? selectedFile.name : 'No file chosen'}
                            </span>
                        </div>

                        <div className="action-row">
                            <button
                                className="analyze-button"
                                onClick={handleUpload}
                                disabled={!selectedFile || loading}
                            >
                                {loading ? 'Analyzing...' : 'Analyze Claim'}
                            </button>
                        </div>
                    </div>

                    {error && <div className="error-message">{error}</div>}
                </div>

                <div className="right-panel">
                    {preview ? (
                        <div className="image-preview">
                            <h3>Product Preview</h3>
                            <div className="preview-container">
                                <img src={preview} alt="Selected" />
                            </div>
                        </div>
                    ) : (
                        <div className="preview-placeholder">
                            <p>No image selected for preview</p>
                        </div>
                    )}
                </div>
            </div>

            {analysis && (
                <div className="analysis-result">
                    <h3>Analysis Result</h3>
                    <div className="analysis-card">
                        {analysis.raw ? (
                            <pre>{analysis.raw}</pre>
                        ) : (
                            <div className="structured-analysis">
                                <div className={`status-badge ${analysis.can_be_claimed ? 'status-accepted' : 'status-rejected'}`}>
                                    {analysis.can_be_claimed ? '✅ Reklamacja możliwa' : '❌ Reklamacja odrzucona'}
                                </div>
                                <div className="analysis-field">
                                    <strong>Rekomendacja:</strong> {analysis.recommendation}
                                </div>
                                <div className="analysis-field">
                                    <strong>Pewność:</strong> {analysis.confidence}%
                                </div>
                                <div className="analysis-field">
                                    <strong>Typ defektu:</strong> {analysis.defect_type}
                                </div>
                                <div className="analysis-field">
                                    <strong>Stan produktu:</strong> {analysis.product_condition}
                                </div>
                                <div className="analysis-field">
                                    <strong>Powód:</strong>
                                    <p>{analysis.reason}</p>
                                </div>
                                {analysis.detected_issues && analysis.detected_issues.length > 0 && (
                                    <div className="analysis-field">
                                        <strong>Wykryte problemy:</strong>
                                        <ul>
                                            {analysis.detected_issues.map((issue, idx) => (
                                                <li key={idx}>{issue}</li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                                {analysis.manual_review_reason && (
                                    <div className="analysis-field warning">
                                        <strong>Powód weryfikacji ręcznej:</strong>
                                        <p>{analysis.manual_review_reason}</p>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default ImageUpload;
