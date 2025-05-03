const express = require('express');
const mysql = require('mysql2');
const dotenv = require('dotenv');
const cors = require('cors');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { OAuth2Client } = require('google-auth-library');

dotenv.config();

const app = express();
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

app.use(cors());
app.use(express.json());

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_DATABASE
});

db.connect((err) => {
    if (err) {
        console.error('Error connecting to the database:', err);
        return;
    }
    console.log('Connected to database.');
});

app.post('/register', async (req, res) => {
    const { name, email, password } = req.body;

    db.query('SELECT * FROM users WHERE email = ?', [email], async (err, results) => {
        if (err) {
            console.error('Error checking email:', err);
            return res.status(500).json({ success: false, message: 'Gagal register' });
        }

        if (results.length > 0) {
            return res.status(400).json({ success: false, message: 'Email sudah terdaftar' });
        }

        try {
            const hashedPassword = await bcrypt.hash(password, 10);

            db.query(
                'INSERT INTO users (name, email, password) VALUES (?, ?, ?)',
                [name, email, hashedPassword],
                (err, result) => {
                    if (err) {
                        console.error('Error inserting user:', err);
                        return res.status(500).json({ success: false, message: 'Gagal register' });
                    }
                    res.status(201).json({ success: true, message: 'Akun berhasil didaftarkan' });
                }
            );
        } catch (err) {
            console.error('Error:', err);
            res.status(500).json({ success: false, message: 'Server error' });
        }
    });
});


app.post('/login', (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ success: false, message: 'Email dan password wajib diisi', token: '' });
    }

    db.query(
        'SELECT * FROM users WHERE email = ?',
        [email],
        async (err, results) => {
            if (err) {
                console.error('Error fetching user:', err);
                return res.status(500).json({ success: false, message: 'Server error', token: '' });
            }

            if (results.length === 0) {
                return res.status(404).json({ success: false, message: 'Email tidak ditemukan', token: '' });
            }

            const user = results[0];

            const validPassword = await bcrypt.compare(password, user.password);
            if (!validPassword) {
                return res.status(401).json({ success: false, message: 'Password salah', token: '' });
            }

            const token = jwt.sign(
                { id: user.id, email: user.email },
                process.env.JWT_SECRET,
                { expiresIn: '1h' }
            );

            res.status(200).json({
                success: true, message: 'Login berhasil', token: token, user: {
                    id: user.id, name: user.name, email: user.email
                }
            });
        }
    );
});

app.post('/auth/google', async (req, res) => {
    const { idToken } = req.body;
    if (!idToken) return res.status(400).json({ success: false, message: "ID Token tidak diberikan" });

    try {
        const ticket = await client.verifyIdToken({
            idToken,
            audience: process.env.GOOGLE_CLIENT_ID,
        });

        const payload = ticket.getPayload();
        const { email, name } = payload;

        db.query('SELECT * FROM users WHERE email = ?', [email], async (err, results) => {
            if (err) return res.status(500).json({ success: false, message: "Database error" });

            if (results.length === 0) {
                db.query('INSERT INTO users (name, email) VALUES (?, ?)', [name, email], (insertErr) => {
                    if (insertErr) return res.status(500).json({ success: false, message: "Gagal menyimpan user" });
                });
            }

            const token = jwt.sign({ email, name }, process.env.JWT_SECRET, { expiresIn: '1h' });
            res.status(200).json({
                success: true,
                message: "Login Google berhasil",
                token,
                name,
                email
            });
        });

    } catch (error) {
        console.error('Google login error:', error);
        res.status(401).json({ success: false, message: "Google Login gagal" });
    }
});


const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
