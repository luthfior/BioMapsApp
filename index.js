const express = require('express');
const mysql = require('mysql2');
const dotenv = require('dotenv');
const cors = require('cors');
const bcrypt = require('bcrypt');

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
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
            return res.status(500).json({ message: 'Gagal register' });
        }

        if (results.length > 0) {
            return res.status(400).json({ message: 'Email sudah terdaftar' });
        }

        try {
            const hashedPassword = await bcrypt.hash(password, 10);

            db.query(
                'INSERT INTO users (name, email, password) VALUES (?, ?, ?)',
                [name, email, hashedPassword],
                (err, result) => {
                    if (err) {
                        console.error('Error inserting user:', err);
                        return res.status(500).json({ message: 'Gagal register' });
                    }
                    res.status(201).json({ message: 'User berhasil didaftarkan' });
                }
            );
        } catch (err) {
            console.error('Error:', err);
            res.status(500).json({ message: 'Server error' });
        }
    });
});


app.post('/login', (req, res) => {
    const { email, password } = req.body;

    db.query(
        'SELECT * FROM users WHERE email = ?',
        [email],
        async (err, results) => {
            if (err) {
                console.error('Error fetching user:', err);
                return res.status(500).json({ message: 'Server error' });
            }

            if (results.length === 0) {
                return res.status(404).json({ message: 'Email tidak ditemukan' });
            }

            const user = results[0];

            const validPassword = await bcrypt.compare(password, user.password);
            if (!validPassword) {
                return res.status(401).json({ message: 'Password salah' });
            }

            res.status(200).json({ message: 'Login berhasil', user: { id: user.id, name: user.name, email: user.email } });
        }
    );
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
