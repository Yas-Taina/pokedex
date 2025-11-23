const pool = require('../config/database');

exports.registerPokemon = async (req, res) => {
  const { pokemon_id, pokemon_name, types, ability, moves, user_login } = req.body;

  if (!pokemon_id || !pokemon_name || !types || !ability || !moves || !user_login) {
    return res.status(400).json({ error: 'Todos os campos são obrigatórios' });
  }

  try {
    const result = await pool.query(
      `INSERT INTO registered_pokemons 
       (pokemon_id, pokemon_name, types, ability, moves, user_login) 
       VALUES ($1, $2, $3, $4, $5, $6) 
       RETURNING *`,
      [pokemon_id, pokemon_name, types, ability, moves, user_login]
    );

    res.status(201).json({
      message: 'Pokémon registrado com sucesso',
      pokemon: result.rows[0]
    });
  } catch (error) {
    if (error.code === '23505') {
      return res.status(409).json({ error: 'Pokémon já registrado por este usuário' });
    }
    console.error('Erro ao registrar pokémon:', error);
    res.status(500).json({ error: 'Erro ao registrar pokémon' });
  }
};

exports.getUserPokemons = async (req, res) => {
  const { user_login } = req.params;

  try {
    const result = await pool.query(
      'SELECT * FROM registered_pokemons WHERE user_login = $1 ORDER BY pokemon_id',
      [user_login]
    );

    res.json(result.rows);
  } catch (error) {
    console.error('Erro ao buscar pokémons:', error);
    res.status(500).json({ error: 'Erro ao buscar pokémons' });
  }
};

exports.checkRegisteredPokemons = async (req, res) => {
  const { user_login } = req.params;

  try {
    const result = await pool.query(
      'SELECT pokemon_id FROM registered_pokemons WHERE user_login = $1',
      [user_login]
    );

    const registeredIds = result.rows.map(row => row.pokemon_id);
    res.json(registeredIds);
  } catch (error) {
    console.error('Erro ao verificar pokémons:', error);
    res.status(500).json({ error: 'Erro ao verificar pokémons' });
  }
};

exports.updatePokemon = async (req, res) => {
  const { id } = req.params;
  const { ability, moves } = req.body;

  if (!ability || !moves) {
    return res.status(400).json({ error: 'Habilidade e movimentos são obrigatórios' });
  }

  try {
    const result = await pool.query(
      'UPDATE registered_pokemons SET ability = $1, moves = $2 WHERE id = $3 RETURNING *',
      [ability, moves, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Pokémon não encontrado' });
    }

    res.json({
      message: 'Pokémon atualizado com sucesso',
      pokemon: result.rows[0]
    });
  } catch (error) {
    console.error('Erro ao atualizar pokémon:', error);
    res.status(500).json({ error: 'Erro ao atualizar pokémon' });
  }
};

exports.deletePokemon = async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(
      'DELETE FROM registered_pokemons WHERE id = $1 RETURNING *',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Pokémon não encontrado' });
    }

    res.json({ message: 'Pokémon deletado com sucesso' });
  } catch (error) {
    console.error('Erro ao deletar pokémon:', error);
    res.status(500).json({ error: 'Erro ao deletar pokémon' });
  }
};

exports.getPokemonDetails = async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(
      'SELECT * FROM registered_pokemons WHERE id = $1',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Pokémon não encontrado' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Erro ao buscar detalhes:', error);
    res.status(500).json({ error: 'Erro ao buscar detalhes' });
  }
};

exports.getHomeStats = async (req, res) => {
  const { user_login } = req.params;

  try {
    const totalResult = await pool.query(
      'SELECT COUNT(*) as total FROM registered_pokemons WHERE user_login = $1',
      [user_login]
    );

    const typesResult = await pool.query(
      `SELECT unnest(string_to_array(types, ',')) as type, COUNT(*) as count
       FROM registered_pokemons 
       WHERE user_login = $1
       GROUP BY type
       ORDER BY count DESC
       LIMIT 3`,
      [user_login]
    );

    const abilitiesResult = await pool.query(
      `SELECT ability, COUNT(*) as count
       FROM registered_pokemons 
       WHERE user_login = $1
       GROUP BY ability
       ORDER BY count DESC
       LIMIT 3`,
      [user_login]
    );

    res.json({
      total: parseInt(totalResult.rows[0].total),
      topTypes: typesResult.rows,
      topAbilities: abilitiesResult.rows
    });
  } catch (error) {
    console.error('Erro ao buscar estatísticas:', error);
    res.status(500).json({ error: 'Erro ao buscar estatísticas' });
  }
};

exports.searchByType = async (req, res) => {
  const { user_login, type } = req.params;

  try {
    const result = await pool.query(
      `SELECT * FROM registered_pokemons 
       WHERE user_login = $1 AND types LIKE $2
       ORDER BY pokemon_id`,
      [user_login, `%${type}%`]
    );

    res.json(result.rows);
  } catch (error) {
    console.error('Erro ao pesquisar por tipo:', error);
    res.status(500).json({ error: 'Erro ao pesquisar por tipo' });
  }
};

exports.searchByAbility = async (req, res) => {
  const { user_login, ability } = req.params;

  try {
    const result = await pool.query(
      `SELECT * FROM registered_pokemons 
       WHERE user_login = $1 AND ability ILIKE $2
       ORDER BY pokemon_id`,
      [user_login, `%${ability}%`]
    );

    res.json(result.rows);
  } catch (error) {
    console.error('Erro ao pesquisar por habilidade:', error);
    res.status(500).json({ error: 'Erro ao pesquisar por habilidade' });
  }
};