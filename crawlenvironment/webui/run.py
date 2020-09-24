"""Run the flask server"""
from app import APP

# Run Server
if __name__ == '__main__':
    APP.run(host='0.0.0.0', port=5000, debug=True, threaded=True)
