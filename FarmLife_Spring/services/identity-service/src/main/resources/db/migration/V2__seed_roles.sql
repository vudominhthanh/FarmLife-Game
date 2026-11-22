INSERT INTO identity.roles(name) VALUES
    ('player'),('moderator'),('gm'),('admin')
ON CONFLICT (name) DO NOTHING;
